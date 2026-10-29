package com.wpits.document.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class PdfDocumentServiceImpl implements PdfDocumentService {

    private final CustomerBillClient customerBillClient;
    private final JasperCustomerBillPdfGenerator pdfGenerator;
    private final Path outputDirectory;

    public PdfDocumentServiceImpl(CustomerBillClient customerBillClient,
                                  JasperCustomerBillPdfGenerator pdfGenerator,
                                  @Value("${document.pdf.output-directory}") String outputDirectory) {
        this.customerBillClient = customerBillClient;
        this.pdfGenerator = pdfGenerator;
        this.outputDirectory = Path.of(outputDirectory).toAbsolutePath().normalize();
    }

    @Override
    public int generateCustomerBillPdfs(LocalDate billDate) {


        List<JsonNode> bills = customerBillClient.getAllCustomerBillsByBillDate(billDate);
        log.info("today-{} bill count: {}",billDate,bills.size());
        
        if (bills.isEmpty()) {
            log.info("No customer bills found for {}. No PDFs generated.", billDate);
            return 0;
        }

        Path datedOutputDirectory = outputDirectory.resolve(billDate.toString());
        try {
            Files.createDirectories(datedOutputDirectory);  //mkdir ki trh work in java21 mai
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create PDF output directory " + datedOutputDirectory, ex);
        }

        int generatedCount = 0;
        for (JsonNode bill : bills) {

            String billId = text(bill, "id");   //billId milegi yha se

            if ("-".equals(billId)) {
                throw new IllegalStateException("CustomerBill API returned a bill without an id for " + billDate);
            }

            Path pdfPath = datedOutputDirectory.resolve(fileName(billId));  //dated folder name ke under pdf file

            try {
                Files.write(pdfPath, pdfGenerator.generate(bill));  //write
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to write PDF for CustomerBill " + billId + " to " + pdfPath, ex);
            }
            generatedCount++;
        }
        log.info("Generated {} customer bill PDFs for {} in {}", generatedCount, billDate, datedOutputDirectory);
        return generatedCount;
    }

    @Override
    public byte[] generateCustomerBillPdf(String customerBillId) {
        JsonNode bill = customerBillClient.getBill(customerBillId);
        if (bill == null || bill.isNull()) {
            throw new CustomerBillClient.CustomerBillClientException("CustomerBill API returned an empty bill for " + customerBillId);
        }
        return pdfGenerator.generate(bill);
    }

    @Override
    public DocumentGenerationResult generateCustomerBillPdfFile(String customerBillId) {
        JsonNode bill = customerBillClient.getBill(customerBillId);
        if (bill == null || bill.isNull()) {
            throw new CustomerBillClient.CustomerBillClientException(
                    "CustomerBill API returned an empty bill for " + customerBillId);
        }

        String billDate = text(bill, "billDate");
        String billNo = text(bill, "billNo");
        LocalDate parsedBillDate = "-".equals(billDate)
                ? null
                : LocalDate.parse(billDate.substring(0, 10));
        Path targetDirectory = outputDirectory.resolve(
                parsedBillDate == null ? "on-demand" : parsedBillDate.toString()).normalize();
        if (!targetDirectory.startsWith(outputDirectory)) {
            throw new IllegalStateException("Invalid bill date in CustomerBill " + customerBillId);
        }

        try {
            Files.createDirectories(targetDirectory);
            Path pdfPath = targetDirectory.resolve(fileName(customerBillId));
            Files.write(pdfPath, pdfGenerator.generate(bill));
            return new DocumentGenerationResult(customerBillId, billNo, "generated",
                    pdfPath.toString(), "Invoice PDF generated successfully");
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to write PDF for CustomerBill " + customerBillId
                            + " to " + targetDirectory, ex);
        }
    }

    private String fileName(String billId) {
        return "customer-bill-" + billId.replaceAll("[^A-Za-z0-9._-]", "_") + ".pdf";
    }

    private String text(JsonNode node, String field) {

        JsonNode value = node.path(field);
        System.out.println("value: "+ value);
        return value.isMissingNode() || value.isNull() || value.asText().isBlank() ? "-" : value.asText();
    }
}
