package com.pos.api;

import com.pos.dao.InvoiceDao;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.InvoiceForm;
import com.pos.pojo.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InvoiceApi {

    @Autowired
    private RestTemplate restTemplate;   // ✅ instance, not class

    public InvoiceData generateInvoice(Integer orderId, InvoiceForm form) {

        String url = "http://localhost:8081/api/invoice/generate";

        return restTemplate.postForObject(
                url,
                form,
                InvoiceData.class
        );
    }
}



