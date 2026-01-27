package com.pos.dto;

import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.InvoiceForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class InvoiceDto {

    private static final String INVOICE_DIR = "/tmp/invoices/";

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${invoice.service.url}")
    private String invoiceServiceUrl;

    public InvoiceData generate(Integer orderId) throws ApiException {

        // 1️⃣ Build invoice form from order
        InvoiceForm form = orderFlow.buildInvoiceForm(orderId);

        // 2️⃣ Call Invoice App
        String url =
                "http://localhost:8081/api/invoice/generate";

        InvoiceData data =
                restTemplate.postForObject(url, form, InvoiceData.class);




        if (data == null || data.getBase64Pdf() == null) {
            throw new ApiException("Failed to generate invoice");
        }

        // 3️⃣ Decode Base64 → PDF
        byte[] pdfBytes =
                Base64.getDecoder().decode(data.getBase64Pdf());

        // 4️⃣ Save PDF
        try {
            Files.createDirectories(Paths.get(INVOICE_DIR));
            Path path = Paths.get(INVOICE_DIR + "INV-" + orderId + ".pdf");
            Files.write(path, pdfBytes);

            // 5️⃣ Attach invoice & mark invoiced
            orderFlow.attachInvoice(orderId, path.toString());

        } catch (Exception e) {
            throw new ApiException("Failed to store invoice PDF", e);
        }

        return data;
    }

    public byte[] download(Integer orderId) throws ApiException {

        // 1️⃣ Validate order + get path
        String path = orderFlow.getInvoicePath(orderId);

        if (path == null) {
            throw new ApiException("Invoice not generated yet for order: " + orderId);
        }

        // 2️⃣ Read PDF from disk
        try {
            Path pdfPath = Paths.get(path);
            return Files.readAllBytes(pdfPath);
        } catch (IOException e) {
            throw new ApiException("Failed to read invoice PDF");
        }
    }

}
