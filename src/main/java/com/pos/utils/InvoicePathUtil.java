package com.pos.utils;

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
        return invoiceDir(invoiceDirPath).resolve(invoiceFileName(orderId));
    }
}
