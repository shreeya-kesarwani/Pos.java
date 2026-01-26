package com.pos.api;

import com.pos.exception.ApiException;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.InvoiceForm;
import com.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InvoiceApi {

    @Autowired
    private RestTemplate restTemplate;

    public InvoiceData generate(InvoiceForm form) throws ApiException {
        try {
            return restTemplate.postForObject(
                    "http://localhost:8081/invoice/generate",
                    form,
                    InvoiceData.class
            );
        } catch (Exception e) {
            e.printStackTrace();   // 👈 ADD THIS
            throw new ApiException("Failed to generate invoice");
        }

    }
}
