//package com.pos.flow;
//
//import com.pos.exception.ApiException;
//import com.pos.model.data.OrderStatus;
//import com.pos.model.form.OrderForm;
//import com.pos.pojo.OrderItemPojo;
//import com.pos.pojo.OrderPojo;
//import com.pos.pojo.ProductPojo;
//import com.pos.service.*;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.time.ZonedDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Component
//public class OrderFlow {
//
//    @Autowired private OrderService orderService;
//    @Autowired private OrderItemService orderItemService;
//    @Autowired private ProductService productService;
//    @Autowired private InventoryService inventoryService;
//    @Autowired private InvoiceService invoiceService;
//
//    @Transactional
//    public void createOrder(OrderForm form) {
//
//        Map<String, ProductPojo> productMap =
//                productService.getByBarcodes(
//                        form.getItems().stream()
//                                .map(i -> i.getBarcode())
//                                .collect(Collectors.toList()));
//
//        inventoryService.validate(productMap, form.getItems());
//
//        OrderPojo order = orderService.create();
//
//        orderItemService.create(
//                order.getId(), form.getItems(), productMap);
//
//        inventoryService.reduce(productMap, form.getItems());
//    }
//
//    public List<OrderPojo> search(Integer id,
//                                  ZonedDateTime start,
//                                  ZonedDateTime end,
//                                  String status) {
//
//        OrderStatus s = (status == null || status.isEmpty())
//                ? null
//                : OrderStatus.valueOf(status);
//
//        return orderService.search(id, start, end, s);
//    }
//
//    public List<OrderItemPojo> getItemsByOrderId(Integer orderId) {
//        return orderItemService.getByOrderId(orderId);
//    }
//
//    public ProductPojo getProductCheck(Integer productId) {
//        return productService.getCheck(productId);
//    }
//
//    @Transactional
//    public void generateInvoice(Integer orderId) throws Exception {
//
//        OrderPojo order = orderService.getCheck(orderId);
//
//        if (order.getStatus() != OrderStatus.CREATED) {
//            throw new ApiException("Invoice already generated");
//        }
//
//        List<OrderItemPojo> items =
//                orderItemService.getByOrderId(orderId);
//
//        String base64 = invoiceService.generate(items);
//        invoiceService.save(orderId, base64);
//
//        orderService.updateStatus(orderId, OrderStatus.INVOICED);
//    }
//
//    public byte[] getInvoicePdf(Integer orderId) throws Exception {
//        return invoiceService.getPdf(orderId);
//    }
//}
