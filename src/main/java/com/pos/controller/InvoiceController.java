package com.pos.controller;

import com.pos.dto.InvoiceDto;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.InvoiceForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @PostMapping("/{orderId}")
    public InvoiceData generate(
            @PathVariable Integer orderId,
            @RequestBody InvoiceForm form
    ) {
        return invoiceDto.generate(orderId, form);
    }
}



