package com.pos.controller;

import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.model.data.InvoiceData;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.OrderForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @RequestMapping(method = RequestMethod.POST)
    public Integer create(@RequestBody OrderForm orderForm) throws ApiException {
        return orderDto.create(orderForm);
    }

    @RequestMapping(method = RequestMethod.GET)
    public PaginatedResponse<OrderData> search(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws ApiException {
        return orderDto.search(id, start, end, status, page, size);
    }

    @RequestMapping(value = "/{orderId}/items", method = RequestMethod.GET)
    public List<OrderItemData> getItems(@PathVariable Integer orderId) throws ApiException {
        return orderDto.getItems(orderId);
    }

    @RequestMapping(value = "/{orderId}/invoice", method = RequestMethod.POST)
    public InvoiceData generateInvoice(@PathVariable Integer orderId) throws ApiException {
        return orderDto.generateInvoice(orderId);
    }

    @RequestMapping(
            value = "/{orderId}/invoice/download",
            method = RequestMethod.GET
    )
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Integer orderId) throws ApiException {
        ResponseEntity<byte[]> res = orderDto.downloadInvoice(orderId);

        return ResponseEntity
                .status(res.getStatusCode())
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"invoice_" + orderId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(res.getBody());
    }
}