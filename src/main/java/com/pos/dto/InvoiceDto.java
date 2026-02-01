package com.pos.dto;

import com.pos.client.InvoiceClient;
import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.data.InvoiceData;
import com.pos.model.constants.OrderStatus;
import com.pos.model.form.InvoiceForm;
import com.pos.pojo.Order;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Set;

@Component
public class InvoiceDto {

    private static final String INVOICE_DIR = "/tmp/invoices/";

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private InvoiceClient invoiceClient;

    @Autowired
    private Validator validator;

    public InvoiceData generate(Integer orderId) throws ApiException {

        InvoiceForm form = orderFlow.buildInvoiceForm(orderId);

        Set<ConstraintViolation<InvoiceForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            throw new ApiException(violations.iterator().next().getMessage());
        }

        InvoiceData data = invoiceClient.generate(form);

        if (data == null || data.getBase64Pdf() == null) {
            throw new ApiException("Failed to generate invoice");
        }

        byte[] pdfBytes = Base64.getDecoder().decode(data.getBase64Pdf());

        try {
            Files.createDirectories(Paths.get(INVOICE_DIR));
            Path path = Paths.get(INVOICE_DIR + "INV-" + orderId + ".pdf");
            Files.write(path, pdfBytes);

            orderFlow.attachInvoice(orderId, path.toString());
        } catch (Exception e) {
            throw new ApiException("Failed to store invoice PDF", e);
        }

        return data;
    }

    public byte[] download(Integer orderId) throws ApiException {

        Order order = orderFlow.getOrder(orderId);

        if (order.getStatus() != OrderStatus.INVOICED) {
            throw new ApiException("Invoice not generated yet for order: " + orderId);
        }

        String path = order.getInvoicePath();
        if (path == null || path.trim().isEmpty()) {
            throw new ApiException("Invoice path missing for order: " + orderId);
        }

        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new ApiException("Failed to read invoice file", e);
        }
    }

    public ResponseEntity<byte[]> downloadResponse(Integer orderId) throws ApiException {
        byte[] pdf = download(orderId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=INV-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
