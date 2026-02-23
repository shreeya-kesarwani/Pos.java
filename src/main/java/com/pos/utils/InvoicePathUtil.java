package com.pos.utils;

import com.pos.exception.ApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.pos.model.constants.ErrorMessages.INVOICE_READ_FAILED;

public final class InvoicePathUtil {

    private InvoicePathUtil() {}

    public static Path invoiceDir(String invoiceDirPath) {
        return Paths.get(invoiceDirPath);
    }

    public static String invoiceFileName(Integer orderId) {
        return "INV-" + orderId + ".pdf";
    }

    public static Path invoiceFilePath(String invoiceDirPath, Integer orderId) {
        return invoiceDir(invoiceDirPath)
                .resolve(invoiceFileName(orderId));
    }

    public static byte[] tryReadInvoiceBytes(String path, Integer orderId) throws ApiException {
        if (path == null || path.trim().isEmpty()) return null;

        try {
            Path p = Paths.get(path);
            if (Files.exists(p) && Files.isDirectory(p)) {
                p = invoiceFilePath(p.toString(), orderId);
            }

            if (!Files.exists(p) || Files.isDirectory(p)) return null;
            return Files.readAllBytes(p);

        } catch (IOException e) {
            throw new ApiException(INVOICE_READ_FAILED.value() + "=" + orderId);
        }
    }

    public static void saveInvoiceBytes(String invoiceDirPath, Integer orderId, byte[] bytes)
            throws IOException {

        Path dir = invoiceDir(invoiceDirPath);
        Files.createDirectories(dir);

        Path filePath = invoiceFilePath(invoiceDirPath, orderId);
        Files.write(filePath, bytes);
    }
}
