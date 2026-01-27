package com.pos.flow;

import com.pos.api.InvoiceApi;
import com.pos.api.OrderApi;
import com.pos.api.OrderItemApi;
import com.pos.api.ProductApi;
import com.pos.dao.InvoiceDao;
import com.pos.exception.ApiException;
import com.pos.model.data.InvoiceData;
import com.pos.model.data.OrderStatus;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.pojo.Invoice;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Transactional
public class InvoiceFlow {

    @Autowired
    private InvoiceApi invoiceApi;

    public InvoiceData generate(Integer orderId, InvoiceForm form) {
        return invoiceApi.generateAndSaveInvoice(orderId, form);
    }
}

