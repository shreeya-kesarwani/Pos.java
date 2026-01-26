package com.pos.controller;

import com.pos.dto.InvoiceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @PostMapping("/{orderId}")
    public void generateInvoice(@PathVariable Integer orderId) {
        invoiceDto.generate(orderId);
    }
}
