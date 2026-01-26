package com.pos.dto;

import com.pos.dao.BaseDao;
import com.pos.exception.ApiException;
import com.pos.flow.InvoiceFlow;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.InvoiceForm;
import com.pos.pojo.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceDto {

    @Autowired
    private InvoiceFlow invoiceFlow;

    public InvoiceData generate(Integer orderId, InvoiceForm form) {
        return invoiceFlow.generate(orderId, form);
    }
}

