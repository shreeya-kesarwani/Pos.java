package com.pos.utils;

import com.pos.api.OrderApi;
import com.pos.exception.ApiException;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.pos.model.constants.ErrorMessages.FAILED_TO_STORE_INVOICE_PDF;

public final class InvoiceStorageUtil {

    private InvoiceStorageUtil() {}

    public static String storePdf(String invoiceDir, Integer orderId, byte[] pdfBytes) throws ApiException {
        try {
            Files.createDirectories(InvoicePathUtil.invoiceDir(invoiceDir));
            Path path = InvoicePathUtil.invoiceFilePath(invoiceDir, orderId);
            Files.write(path, pdfBytes);
            return path.toString();
        } catch (Exception e) {
            throw new ApiException(FAILED_TO_STORE_INVOICE_PDF.value() + ": orderId=" + orderId, e);
        }
    }

    public static void storeAndAttach(OrderApi orderApi, String invoiceDir, Integer orderId, byte[] pdfBytes)
            throws ApiException {
        String path = storePdf(invoiceDir, orderId, pdfBytes);
        orderApi.generateInvoice(orderId, path);
    }
}
