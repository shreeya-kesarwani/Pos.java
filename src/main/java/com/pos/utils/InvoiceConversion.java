package com.pos.utils;

import com.pos.api.ProductApi;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.pojo.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class InvoiceConversion {

    public static InvoiceForm toInvoiceForm(
            Integer orderId,
            List<OrderItem> items,
            ProductApi productApi
    ) {
        InvoiceForm form = new InvoiceForm();
        form.setOrderId(orderId);

        List<InvoiceItemForm> invoiceItems = new ArrayList<>();

        for (OrderItem item : items) {
            InvoiceItemForm f = new InvoiceItemForm();
            String name = productApi.getNameById(item.getProductId());

            f.setName(name);
            f.setQuantity(item.getQuantity());
            f.setSellingPrice(item.getSellingPrice());

            invoiceItems.add(f);
        }

        form.setItems(invoiceItems);
        return form;
    }
}
