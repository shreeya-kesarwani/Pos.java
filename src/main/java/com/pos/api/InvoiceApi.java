package com.pos.api;

import com.pos.dao.InvoiceDao;
import com.pos.exception.ApiException;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.InvoiceForm;
import com.pos.pojo.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InvoiceApi {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InvoiceDao invoiceDao;

    public InvoiceData generateAndSaveInvoice(Integer orderId, InvoiceForm form)
            throws ApiException {

        String url = "http://localhost:8081/api/invoice/generate/" + orderId;

        InvoiceData invoiceData =
                restTemplate.postForObject(url, form, InvoiceData.class);

        if (invoiceData == null) {
            throw new ApiException("Failed to generate invoice");
        }

        Invoice invoice = new Invoice();
        invoice.setOrderId(orderId);
        invoice.setPath(invoiceData.getPdfPath());

        invoiceDao.insert(invoice);

        return invoiceData;
    }
}
