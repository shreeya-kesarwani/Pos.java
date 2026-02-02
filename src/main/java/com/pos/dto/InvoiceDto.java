package com.pos.dto;

import com.pos.client.InvoiceClient;
import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
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
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;

@Component
public class InvoiceDto {

    private static final String INVOICE_DIR = "/tmp/invoices/";

    @Autowired OrderFlow orderFlow;
    @Autowired InvoiceClient invoiceClient;
    @Autowired Validator validator;

    public InvoiceData generate(Integer orderId) throws ApiException {

        Order order = orderFlow.getOrder(orderId);
        if (order.getStatus() == OrderStatus.INVOICED) {
            throw new ApiException("Order already invoiced");
        }

        List<OrderItem> items = orderFlow.getOrderItems(orderId);
        if (items.isEmpty()) {
            throw new ApiException("Cannot invoice an empty order");
        }

        Set<Integer> productIds = items.stream()
                .map(OrderItem::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Product> products = orderFlow.getProductsByIds(productIds);
        Map<Integer, Product> productById = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        InvoiceForm form = new InvoiceForm();
        form.setOrderId(orderId);

        List<InvoiceItemForm> invoiceItems = items.stream().map(item -> {
            Product p = productById.get(item.getProductId());
            if (p == null) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }

            InvoiceItemForm f = new InvoiceItemForm();
            f.setName(p.getName());
            f.setQuantity(item.getQuantity());
            f.setSellingPrice(item.getSellingPrice());
            return f;
        }).toList();

        form.setItems(invoiceItems);

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
        if (path == null || path.isBlank()) {
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
