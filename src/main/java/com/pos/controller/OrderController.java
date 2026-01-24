//package com.pos.controller;
//
//import com.pos.dto.OrderDto;
//import com.pos.model.data.OrderData;
//import com.pos.model.data.OrderItemData;
//import com.pos.model.form.OrderForm;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.ZonedDateTime;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/orders")
//public class OrderController {
//
//    @Autowired
//    private OrderDto orderDto;
//
//    @PostMapping
//    public void add(@RequestBody OrderForm form) {
//        orderDto.add(form);
//    }
//
//    @GetMapping
//    public List<OrderData> search(
//            @RequestParam(required = false) Integer id,
//            @RequestParam(required = false)
//            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
//            @RequestParam(required = false)
//            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end,
//            @RequestParam(required = false) String status) {
//
//        return orderDto.search(id, start, end, status);
//    }
//
//    @GetMapping("/{id}/items")
//    public List<OrderItemData> getItems(@PathVariable Integer id) {
//        return orderDto.getItems(id);
//    }
//
//    @PostMapping("/{id}/invoice")
//    public void generateInvoice(@PathVariable Integer id) throws Exception {
//        orderDto.generateInvoice(id);
//    }
//
//    @GetMapping("/{id}/invoice")
//    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Integer id) throws Exception {
//        byte[] pdf = orderDto.getInvoicePdf(id);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        headers.setContentDispositionFormData(
//                "attachment", "invoice_" + id + ".pdf");
//
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body(pdf);
//    }
//}
