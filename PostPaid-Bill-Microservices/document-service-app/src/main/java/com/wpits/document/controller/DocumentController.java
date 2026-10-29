package com.wpits.document.controller;

import com.wpits.document.service.PdfDocumentService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final PdfDocumentService pdfDocumentService;

    @PostMapping("/customer-bills")
    public ResponseEntity<BatchGenerationResponse> generateCustomerBillPdfs(
            @RequestParam(name = "billDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billDate) {
        //LocalDate billDate = requestedBillDate == null ? LocalDate.now() : requestedBillDate;
        int generatedCount = pdfDocumentService.generateCustomerBillPdfs(billDate);
        return ResponseEntity.ok(new BatchGenerationResponse(billDate, generatedCount));
    }

    @PostMapping("/customer-bills/{customerBillId}/generate")
    public ResponseEntity<PdfDocumentService.DocumentGenerationResult> generateCustomerBillPdfFile(
            @PathVariable @NotBlank String customerBillId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pdfDocumentService.generateCustomerBillPdfFile(customerBillId));
    }

    @GetMapping("/customer-bills/{customerBillId}.pdf")
    public ResponseEntity<byte[]> customerBillPdf(
            @PathVariable @NotBlank String customerBillId) {
        byte[] pdf = pdfDocumentService.generateCustomerBillPdf(customerBillId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("customer-bill-" + customerBillId + ".pdf")
                .build());
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    public record BatchGenerationResponse(LocalDate billDate, int generatedCount) {
    }
}
