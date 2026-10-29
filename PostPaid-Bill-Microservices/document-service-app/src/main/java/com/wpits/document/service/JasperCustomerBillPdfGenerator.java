package com.wpits.document.service;

import com.wpits.document.helpers.AmountToWordsConverter;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

@Component
public class JasperCustomerBillPdfGenerator {

    private static final String COMPILER_CLASSPATH_PROPERTY = "net.sf.jasperreports.compiler.classpath";
    private final SimulatorAddressClient simulatorAddressClient;
    private final JasperReport report;
    private final byte[] logo;
    private final String defaultText;
    private final BigDecimal defaultAmount;

    @Autowired
    private AmountToWordsConverter amountToWordsConverter;

    public JasperCustomerBillPdfGenerator(
            SimulatorAddressClient simulatorAddressClient,
            @Value("${document.pdf.template-path}") String templatePath,
            @Value("${document.pdf.logo-path}") String logoPath,
            @Value("${document.bill.defaults.text:N/A}") String defaultText,
            @Value("${document.bill.defaults.amount:0}") BigDecimal defaultAmount) {
        this.simulatorAddressClient = simulatorAddressClient;
        this.defaultText = defaultText;
        this.defaultAmount = defaultAmount;
        try {
            configureNestedJarCompilerClasspath();
            try (InputStream template = Files.newInputStream(Path.of(templatePath))) {
                this.report = JasperCompileManager.compileReport(template);
            }
            this.logo = Files.readAllBytes(Path.of(logoPath));
        } catch (IOException | JRException ex) {
            throw new IllegalStateException("Unable to load the customer bill Jasper template or logo", ex);
        }
    }

    private void configureNestedJarCompilerClasspath() throws IOException {
        var jasperContext = DefaultJasperReportsContext.getInstance();
        String configuredClasspath = jasperContext.getProperty(COMPILER_CLASSPATH_PROPERTY);
        if (configuredClasspath == null || configuredClasspath.isBlank()) {
            configuredClasspath = System.getProperty(COMPILER_CLASSPATH_PROPERTY, "");
        }
        String resourceUrl = JasperCompileManager.class
                .getResource("JasperCompileManager.class").toExternalForm();
        if (!resourceUrl.startsWith("jar:nested:")
                || configuredClasspath.contains("jasperreports-")) {
            return;
        }

        int outerJarEnd = resourceUrl.indexOf("/!", "jar:nested:".length());
        int nestedJarStart = outerJarEnd + 2;
        int nestedJarEnd = resourceUrl.indexOf("!/", nestedJarStart);
        if (outerJarEnd < 0 || nestedJarEnd < 0) {
            throw new IOException("Unable to resolve JasperReports library inside the executable archive");
        }

        String outerJarPath = resourceUrl.substring("jar:nested:".length(), outerJarEnd);
        if (outerJarPath.matches("^/[A-Za-z]:/.*")) {
            outerJarPath = outerJarPath.substring(1);
        }
        String nestedJarEntry = resourceUrl.substring(nestedJarStart, nestedJarEnd);
        Path compilerJar = Files.createTempFile("jasperreports-compiler-", ".jar");
        try (JarFile executableJar = new JarFile(outerJarPath)) {
            var entry = executableJar.getJarEntry(nestedJarEntry);
            if (entry == null) {
                throw new IOException("JasperReports library is missing from the executable archive");
            }
            try (InputStream jarContents = executableJar.getInputStream(entry)) {
                Files.copy(jarContents, compilerJar, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        compilerJar.toFile().deleteOnExit();

        String classpath = System.getProperty("java.class.path", "");
        jasperContext.setProperty(COMPILER_CLASSPATH_PROPERTY,
                (configuredClasspath.isBlank() ? classpath : configuredClasspath)
                        + java.io.File.pathSeparator + compilerJar);
    }

    public byte[] generate(JsonNode bill) {

        Map<String, Object> fields = reportFields(bill); //value to put in jasper

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("logo", new java.io.ByteArrayInputStream(logo));

        try {
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JRMapArrayDataSource(new Object[]{fields}));
            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException ex) {
            throw new IllegalStateException("Unable to generate PDF for CustomerBill " + string(bill, "id"), ex);
        }
    }

    private Map<String, Object> reportFields(JsonNode bill) {

        //JsonNode billingAccount = bill.get("billingAccount").get("name");
        //System.out.println("billingAccount: "+ billingAccount);


        Map<String, Object> fields = new HashMap<>();

        JsonNode relatedParty = firstArrayElement(bill.path("relatedParty"));
        JsonNode party = relatedParty.path("partyOrPartyRole");
        /*String partyId = text(party, "partyId");
        if (partyId.equals(defaultText)) {
            partyId = text(party, "id");
        }*/
        String partyId = text(party, "id");
        System.out.println("############### partyId: "+ partyId);
        //chek null,empty,NA -> defaultText
        JsonNode address = partyId == null || partyId.isEmpty() || partyId.equals(defaultText) ? null : simulatorAddressClient.getAddressDetails(partyId);
        JsonNode billingPeriod = bill.path("billingPeriod");
        JsonNode items = bill.path("billItem");

        JsonNode amountDueNode = bill.path("amountDue").path("value");
        JsonNode taxExcludedNode = bill.path("taxExcludedAmount").path("value");
        JsonNode taxIncludedNode = bill.path("taxIncludedAmount").path("value");
        BigDecimal amountDue = decimal(amountDueNode);
        BigDecimal taxExcluded = decimal(taxExcludedNode);
        BigDecimal taxIncluded = decimal(taxIncludedNode);
        BigDecimal itemTax = sumTaxes(bill.path("taxItem"));

        BigDecimal taxAmount = hasValue(taxIncludedNode) && hasValue(taxExcludedNode) ? taxIncluded.subtract(taxExcluded) : itemTax;

        BigDecimal totalCharges = hasValue(taxIncludedNode) ? taxIncluded : hasValue(taxExcludedNode) ? taxExcluded.add(taxAmount) : sumItems(items, description -> true);

        BigDecimal taxableValue = hasValue(taxExcludedNode) ? taxExcluded : totalCharges.subtract(taxAmount);

        BigDecimal lateFee = sumItems(items, description -> containsAny(description, "late fee", "late payment"));
        BigDecimal payable = hasValue(amountDueNode) ? amountDue : totalCharges;
        Date billCycleDate = date(bill.path("billCycle").path("billingDate"));
        if (billCycleDate == null) {
            billCycleDate = date(bill.path("nextBillDate"));
        }
        if (billCycleDate == null) {
            billCycleDate = date(bill.path("billDate"));
        }
        /*String serviceDescription = text(bill, "category");
        if (serviceDescription.equals(defaultText)) {
            serviceDescription = text(firstArrayElement(items), "description");
        }*/

        fields.put("customerName", bill.get("billingAccount").get("name").toString());
        fields.put("customerAddressLine1", address == null ? "NA" : text(address, "addressLine1"));
        fields.put("customerAddressLine2", address == null ? "NA" : text(address, "addressLine2"));
        fields.put("customerAddressLine3", address == null ? "NA" : text(address, "addressLine3"));
        fields.put("mobileNumber", text(firstArrayElement(party.path("contactMedium")).path("medium"), "phoneNumber"));
        fields.put("accountNumber", text(bill.path("billingAccount"), "id"));
        fields.put("planName", planName(items, bill.path("billCycle")));
        fields.put("creditLimit", defaultAmount);
        fields.put("securityDeposit", defaultAmount);
        fields.put("alternateContactNumber", defaultText);
        fields.put("billCycleDate", billCycleDate);
        fields.put("invoiceDate", date(bill.path("billDate")));
        fields.put("billPeriod", period(billingPeriod));
        fields.put("invoiceNumber", text(bill, "billNo"));
        fields.put("serviceDescription", "Telecommunications");
        fields.put("placeOfSupply", "New Delhi");
        fields.put("stateCode", "07");
        fields.put("customerGstNo", "-");
        fields.put("totalPayableAmount", payable);
        fields.put("dueDate", date(bill.path("paymentDueDate")));
        fields.put("previousBalance", defaultAmount);
        fields.put("paymentsLastMonth", appliedPayments(bill.path("appliedPayment")));
        fields.put("adjustments", defaultAmount);
        fields.put("payableAfterDueDate", payable.add(lateFee));
        fields.put("payableAfterDate", null);
        fields.put("lateFeeAmount", lateFee);
        fields.put("oneTimeCharges", sumItems(items, description -> containsAny(description, "one time", "one-time", "activation", "installation")));
        fields.put("monthlyCharges", sumItems(items, description -> containsAny(description, "monthly", "recurring", "rental")));
        fields.put("usageCharges", sumItems(items, description -> containsAny(description, "usage", "call", "sms", "data", "roaming")));
        fields.put("callCharges", sumItems(items, description -> description.contains("call") && !description.contains("conference")));
        fields.put("conferenceCallCharges", sumItems(items, description -> description.contains("conference")));
        fields.put("smsCharges", sumItems(items, description -> description.contains("sms")));
        fields.put("mobileInternetCharges", sumItems(items, description -> containsAny(description, "internet", "data")));
        fields.put("nationalRoamingCharges", sumItems(items, description -> description.contains("national roaming")));
        fields.put("valueAddedServiceCharges", sumItems(items, description -> description.contains("value added")));
        fields.put("internationalRoamingCharges", sumItems(items, description -> description.contains("international roaming")));
        fields.put("discounts", sumItems(items, description -> description.contains("discount")));
        fields.put("otherCreditsCharges", defaultAmount);
        fields.put("miscellaneousCharges", sumItems(items, description -> description.contains("miscellaneous")));
        fields.put("latePaymentCharges", lateFee);
        fields.put("taxableValue", taxableValue);
        fields.put("taxAmount", taxAmount);
        fields.put("totalCurrentCharges", totalCharges);
        fields.put("amountInWords", amountToWordsConverter.convertAmountToWords(totalCharges));
        fields.put("invoiceRefNo", text(bill, "billNo"));

        return fields;
    }

    private String planName(JsonNode items, JsonNode billCycle) {
        for (JsonNode item : items) {
            String planName = text(item, "planName");
            if (!defaultText.equals(planName)) {
                return planName;
            }
        }
        return text(billCycle, "name");
    }

    private String period(JsonNode billingPeriod) {

        String start = dateText(billingPeriod.path("startDateTime"));
        String end = dateText(billingPeriod.path("endDateTime"));

        return start.equals(defaultText) || end.equals(defaultText)  ? defaultText : start + " to " + end;
    }

    private String dateText(JsonNode value) {
        Date date = date(value);
        return date == null
                ? defaultText
                : DateTimeFormatter.ofPattern("dd MMM yyyy")
                        .withZone(ZoneId.systemDefault()).format(date.toInstant());
    }

    private Date date(JsonNode value) {
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            return null;
        }
        String text = value.asText();
        try {
            return Date.from(LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignored) {
            try {
                return Date.from(OffsetDateTime.parse(text).toInstant());
            } catch (DateTimeParseException ignoredOffset) {
                try {
                    return Date.from(LocalDate.parse(text).atStartOfDay(ZoneId.systemDefault()).toInstant());
                } catch (DateTimeParseException ignoredDate) {
                    return null;
                }
            }
        }
    }

    private BigDecimal money(JsonNode money) {

        return decimal(money.path("value"));
    }

    private boolean hasValue(JsonNode value) {

        return !value.isMissingNode() && !value.isNull() && !value.asText().isBlank();
    }

    private BigDecimal decimal(JsonNode value) {

        if (value.isNumber()) {
            return value.decimalValue();
        }
        if (!value.isMissingNode() && !value.isNull() && !value.asText().isBlank()) {
            try {
                return new BigDecimal(value.asText());
            } catch (NumberFormatException ignored) {
                return defaultAmount;
            }
        }
        return defaultAmount;
    }

    private BigDecimal appliedPayments(JsonNode payments) {
        if (!payments.isArray() || payments.isEmpty()) {
            return defaultAmount;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (JsonNode payment : payments) {
            sum = sum.add(money(payment.path("appliedAmount")));
        }
        return sum;
    }

    private BigDecimal sumTaxes(JsonNode taxes) {
        if (!taxes.isArray() || taxes.isEmpty()) {
            return defaultAmount;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (JsonNode tax : taxes) {
            sum = sum.add(money(tax.path("taxAmount")));
        }
        return sum;
    }

    private BigDecimal sumItems(JsonNode items, java.util.function.Predicate<String> include) {

        if (!items.isArray() || items.isEmpty()) {
            return defaultAmount; //zero de dega
        }

        BigDecimal sum = BigDecimal.ZERO;
        boolean matched = false;
        for (JsonNode item : items) {
            if (include.test(string(item, "description").toLowerCase())) {
                matched = true;
                sum = sum.add(decimal(item.path("amount")));
            }
        }
        return matched ? sum : defaultAmount;
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private JsonNode firstArrayElement(JsonNode array) {

        return array.path(0);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() || value.asText().isBlank() ? defaultText : value.asText();
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (!value.equals(defaultText)) {
                return value;
            }
        }
        return defaultText;
    }

    private String string(JsonNode node, String field) {
        return text(node, field);
    }

}
