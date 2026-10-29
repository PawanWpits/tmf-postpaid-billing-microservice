package com.wpits.document.service;

import java.time.LocalDate;

public interface PdfDocumentService {

    int generateCustomerBillPdfs(LocalDate billDate);

    byte[] generateCustomerBillPdf(String customerBillId);

    DocumentGenerationResult generateCustomerBillPdfFile(String customerBillId);

    record DocumentGenerationResult(
            String billId,
            String billNo,
            String status,
            String path,
            String message) {
    }
}
