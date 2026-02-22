package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.pos.model.constants.ErrorMessages.PRODUCT_ID_NOT_FOUND;

public class InvoiceConversion {

    private InvoiceConversion() {}

    public static InvoiceForm toInvoiceForm(Integer orderId, List<OrderItem> items, Map<Integer, Product> productById) throws ApiException {

        InvoiceForm form = new InvoiceForm();
        form.setOrderId(orderId);
        form.setItems(toInvoiceItems(items, productById));
        return form;
    }

    public static List<InvoiceItemForm> toInvoiceItems(List<OrderItem> items, Map<Integer, Product> productById) throws ApiException {

        List<InvoiceItemForm> invoiceItems = new ArrayList<>();

        for (OrderItem item : items) {
            Integer productId = item.getProductId();
            Product product = productById.get(productId);

            if (product == null) {
                throw new ApiException(PRODUCT_ID_NOT_FOUND.value());
            }

            InvoiceItemForm invoiceItemForm = new InvoiceItemForm();
            invoiceItemForm.setName(product.getName());
            invoiceItemForm.setQuantity(item.getQuantity());
            invoiceItemForm.setSellingPrice(item.getSellingPrice());
            invoiceItems.add(invoiceItemForm);
        }

        return invoiceItems;
    }

    public static byte[] decodePdfBytes(InvoiceData data) throws ApiException {
        if (data == null || data.getBase64Pdf() == null || data.getBase64Pdf().isBlank()) {
            throw new ApiException("Failed to generate invoice");
        }
        try {
            return Base64.getDecoder().decode(data.getBase64Pdf());
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid invoice PDF received", e);
        }
    }
}
