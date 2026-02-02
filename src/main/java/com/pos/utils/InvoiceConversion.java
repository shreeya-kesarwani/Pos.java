package com.pos.utils;

import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.pojo.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvoiceConversion {

    private InvoiceConversion() {}

    public static InvoiceForm toInvoiceForm(
            Integer orderId,
            List<OrderItem> items,
            Map<Integer, String> productNameById
    ) {
        InvoiceForm form = new InvoiceForm();
        form.setOrderId(orderId);

        List<InvoiceItemForm> invoiceItems = new ArrayList<>();

        for (OrderItem item : items) {
            InvoiceItemForm f = new InvoiceItemForm();

            String name = (productNameById == null)
                    ? null
                    : productNameById.get(item.getProductId());

            f.setName(name == null ? "Unknown Product" : name);
            f.setQuantity(item.getQuantity());
            f.setSellingPrice(item.getSellingPrice());

            invoiceItems.add(f);
        }

        form.setItems(invoiceItems);
        return form;
    }
}
