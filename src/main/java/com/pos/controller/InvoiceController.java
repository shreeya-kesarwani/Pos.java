package com.pos.controller;

import com.pos.dto.InvoiceDto;
import com.pos.exception.ApiException;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.InvoiceForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {
    @Autowired
    private InvoiceDto invoiceDto;

    @RequestMapping(value = "/{orderId}", method = RequestMethod.POST)
    public InvoiceData generate(@PathVariable Integer orderId)
            throws ApiException {
        return invoiceDto.generate(orderId);
    }

    @RequestMapping(
            value = "/{orderId}",
            method = RequestMethod.GET,
            produces = "application/pdf"
    )
    public byte[] download(@PathVariable Integer orderId)
            throws ApiException {

        return invoiceDto.download(orderId);
    }

}




