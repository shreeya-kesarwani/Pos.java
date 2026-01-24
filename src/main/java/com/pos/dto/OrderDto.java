//package com.pos.dto;
//
//import com.pos.exception.ApiException;
//import com.pos.flow.OrderFlow;
//import com.pos.model.data.OrderData;
//import com.pos.model.data.OrderItemData;
//import com.pos.model.form.OrderForm;
//import com.pos.model.form.OrderItemForm;
//import com.pos.pojo.OrderItemPojo;
//import com.pos.pojo.OrderPojo;
//import com.pos.pojo.ProductPojo;
//import com.pos.utils.OrderConversion;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.ZonedDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class OrderDto {
//
//    @Autowired
//    private OrderFlow orderFlow;
//
//    public void add(OrderForm form) {
//        validate(form);
//        normalize(form);
//        orderFlow.createOrder(form);
//    }
//
//    public List<OrderData> search(Integer id,
//                                  ZonedDateTime start,
//                                  ZonedDateTime end,
//                                  String status) {
//
//        List<OrderPojo> orders =
//                orderFlow.search(id, start, end, status);
//
//        return orders.stream().map(order -> {
//            List<OrderItemPojo> items =
//                    orderFlow.getItemsByOrderId(order.getId());
//
//            double total = items.stream()
//                    .mapToDouble(i -> i.getQuantity() * i.getSellingPrice())
//                    .sum();
//
//            return OrderConversion.convertPojoToData(order, total);
//        }).collect(Collectors.toList());
//    }
//
//    public List<OrderItemData> getItems(Integer orderId) {
//        List<OrderItemPojo> items =
//                orderFlow.getItemsByOrderId(orderId);
//
//        List<OrderItemData> data = new ArrayList<>();
//        for (OrderItemPojo p : items) {
//            ProductPojo product =
//                    orderFlow.getProductCheck(p.getProductId());
//
//            data.add(OrderConversion.convertItemPojoToData(
//                    p, product.getBarcode(), product.getName()));
//        }
//        return data;
//    }
//
//    public void generateInvoice(Integer orderId) throws Exception {
//        orderFlow.generateInvoice(orderId);
//    }
//
//    public byte[] getInvoicePdf(Integer orderId) throws Exception {
//        return orderFlow.getInvoicePdf(orderId);
//    }
//
//    /* ---------- helpers ---------- */
//
//    private void validate(OrderForm form) {
//        if (form.getItems() == null || form.getItems().isEmpty()) {
//            throw new ApiException("Order must contain at least one item");
//        }
//
//        Set<String> seen = new HashSet<>();
//        for (OrderItemForm item : form.getItems()) {
//            if (item.getQuantity() <= 0) {
//                throw new ApiException("Quantity must be positive");
//            }
//            if (item.getSellingPrice() < 0) {
//                throw new ApiException("Selling price cannot be negative");
//            }
//            if (!seen.add(item.getBarcode().toLowerCase())) {
//                throw new ApiException("Duplicate barcode: " + item.getBarcode());
//            }
//        }
//    }
//
//    private void normalize(OrderForm form) {
//        for (OrderItemForm item : form.getItems()) {
//            item.setBarcode(item.getBarcode().trim().toLowerCase());
//        }
//    }
//}
