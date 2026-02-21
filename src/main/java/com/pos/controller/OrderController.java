package com.pos.controller;

import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.model.data.InvoiceData;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderSearchForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @RequestMapping(method = RequestMethod.POST)
    public Integer create(@Valid @RequestBody OrderForm orderForm) throws ApiException {
        return orderDto.create(orderForm);
    }

    @RequestMapping(method = RequestMethod.GET)
    public PaginatedResponse<OrderData> search(@ModelAttribute OrderSearchForm form) throws ApiException {
        if (form.getPageNumber() == null) form.setPageNumber(0);
        if (form.getPageSize() == null) form.setPageSize(10);

        return orderDto.search(form);
    }

    @RequestMapping(value = "/{orderId}/items", method = RequestMethod.GET)
    public List<OrderItemData> getItems(@PathVariable Integer orderId) throws ApiException {
        return orderDto.getItems(orderId);
    }

    @RequestMapping(value = "/{orderId}/invoice", method = RequestMethod.POST)
    public InvoiceData generateInvoice(@PathVariable Integer orderId) throws ApiException {
        return orderDto.generateInvoice(orderId);
    }

    @RequestMapping(value = "/{orderId}/invoice/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Integer orderId) throws ApiException {
        ResponseEntity<byte[]> res = orderDto.downloadInvoice(orderId);

        return ResponseEntity
                .status(res.getStatusCode())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"invoice_" + orderId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(res.getBody());
    }
}