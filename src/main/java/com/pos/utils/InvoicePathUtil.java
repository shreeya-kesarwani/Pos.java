package com.pos.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static byte[] tryReadInvoiceBytes(String invoiceDirPath, Integer orderId) {
        if (invoiceDirPath == null || invoiceDirPath.isBlank()) return null;

        Path filePath = invoiceFilePath(invoiceDirPath, orderId);

        if (!Files.exists(filePath) ||
                !Files.isRegularFile(filePath) ||
                !Files.isReadable(filePath)) {
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return (bytes == null || bytes.length == 0) ? null : bytes;
        } catch (IOException e) {
            return null; // fallback to regeneration
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
