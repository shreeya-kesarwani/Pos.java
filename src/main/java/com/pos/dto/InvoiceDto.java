package com.pos.dto;

import com.pos.exception.ApiException;
import com.pos.flow.InvoiceFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceDto extends AbstractDto {

    @Autowired
    private InvoiceFlow invoiceFlow;

    public void generate(Integer orderId) throws ApiException {
        if (orderId == null) {
            throw new ApiException("OrderId cannot be null");
        }
        invoiceFlow.generateInvoice(orderId);
    }
}
