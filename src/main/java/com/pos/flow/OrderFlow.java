package com.pos.flow;

import com.pos.api.InventoryApi;
import com.pos.api.OrderApi;
import com.pos.api.OrderItemApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import com.pos.utils.OrderConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = ApiException.class)
public class OrderFlow {

    @Autowired private OrderApi orderApi;
    @Autowired private OrderItemApi orderItemApi;
    @Autowired private InventoryApi inventoryApi;
    @Autowired private ProductApi productApi;

    public Integer createOrder(OrderForm form) throws ApiException {

        Order order = orderApi.create();

        List<String> barcodes = form.getItems().stream()
                .map(OrderItemForm::getBarcode)
                .toList();

        List<Product> products = productApi.getCheckByBarcodes(barcodes);
        Map<String, Product> productByBarcode = products.stream()
                .collect(Collectors.toMap(Product::getBarcode, p -> p, (a, b) -> a));

        for (OrderItemForm itemForm : form.getItems()) {
            Product product = productByBarcode.get(itemForm.getBarcode());
            if (product == null) throw new ApiException("Product not found: " + itemForm.getBarcode());

            productApi.validateSellingPrice(product.getId(), itemForm.getSellingPrice());
            inventoryApi.allocate(product.getId(), itemForm.getQuantity());
            orderItemApi.add(order.getId(), product.getId(), itemForm.getQuantity(), itemForm.getSellingPrice());
        }

        return order.getId();
    }

    @Transactional(readOnly = true)
    public List<Order> search(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status, int page, int size) {
        return orderApi.search(id, start, end, status, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status) {
        return orderApi.getCount(id, start, end, status);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Integer orderId) throws ApiException {
        return orderApi.getCheck(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItems(Integer orderId) throws ApiException {
        orderApi.getCheck(orderId);
        return orderItemApi.getByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByIds(Set<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        return productApi.getByIds(productIds.stream().toList());
    }

    @Transactional(readOnly = true)
    public Double calculateTotalAmount(Integer orderId) throws ApiException {
        orderApi.getCheck(orderId);
        return OrderConversion.calculateTotalAmount(orderItemApi.getByOrderId(orderId));
    }

    public void attachInvoice(Integer orderId, String path) throws ApiException {
        orderApi.attachInvoice(orderId, path);
    }
}
