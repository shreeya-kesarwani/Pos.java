//package com.pos.utils;
//
//import com.pos.model.data.OrderData;
//import com.pos.model.data.OrderItemData;
//import com.pos.model.form.OrderItemForm;
//import com.pos.pojo.OrderItemPojo;
//import com.pos.pojo.OrderPojo;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class OrderConversion {
//
//    // 1. Convert Form to Pojo (Used during Order Creation)
//    public static OrderItemPojo convertFormToPojo(OrderItemForm form) {
//        OrderItemPojo p = new OrderItemPojo();
//        Integer productId =
//        p.setProductId(productId);
//        p.setQuantity(form.getQuantity());
//        p.setSellingPrice(form.getSellingPrice());
//        return p;
//    }
//
//    // 2. Convert Order Pojo to OrderData (For the main table list)
//    public static OrderData convertPojoToData(OrderPojo p, Double totalAmount) {
//        OrderData d = new OrderData();
//        d.setId(p.getId());
//        d.setCreatedAt(p.getCreatedAt());
//        // FIX: Convert Enum to String
//        d.setStatus(p.getStatus().name());
//        d.setTotalAmount(totalAmount);
//        return d;
//    }
//
//    // 3. Convert OrderItem Pojo to OrderItemData (For the expanded view)
//    public static OrderItemData convertItemPojoToData(OrderItemPojo p, String barcode, String productName) {
//        OrderItemData d = new OrderItemData();
//        d.setId(p.getId());
//        d.setBarcode(barcode);
//        d.setProductName(productName);
//        d.setQuantity(p.getQuantity());
//        d.setSellingPrice(p.getSellingPrice());
//        return d;
//    }
//}