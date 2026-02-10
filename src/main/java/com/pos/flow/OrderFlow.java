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
import com.pos.pojo.Product;
import com.pos.utils.OrderMathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.pos.pojo.OrderItem;
import com.pos.utils.CollectionIndexUtil;

import java.time.ZonedDateTime;
import java.util.*;
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
            orderItemApi.add(
                    order.getId(),
                    product.getId(),
                    itemForm.getQuantity(),
                    itemForm.getSellingPrice()
            );
        }
        return order.getId();
    }

    public Map<String, Object> getOrderItemsWithProducts(Integer orderId) throws ApiException {
        orderApi.getCheck(orderId);

        List<OrderItem> items = orderItemApi.getByOrderId(orderId);
        if (items.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("items", List.of());
            result.put("productById", Map.of());
            return result;
        }

        Set<Integer> productIds = new HashSet<>();
        for (OrderItem item : items) {
            if (item.getProductId() != null) productIds.add(item.getProductId());
        }
        Map<Integer, Product> productById = Map.of();
        if (!productIds.isEmpty()) {
            List<Product> products = productApi.getByIds(new ArrayList<>(productIds));
            productById = CollectionIndexUtil.indexBy(products, Product::getId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("productById", productById);
        return result;
    }

    public Map<String, Object> searchWithTotals(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status, Integer pageNumber, Integer pageSize) throws ApiException {

        List<Order> orders = orderApi.search(id, start, end, status, pageNumber, pageSize);
        long totalCount = orderApi.getCount(id, start, end, status);

        Map<Integer, Double> totals = new HashMap<>();
        for (Order order : orders) {
            List<OrderItem> items = orderItemApi.getByOrderId(order.getId());
            totals.put(order.getId(), OrderMathUtil.calculateTotalAmount(items));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orders", orders);
        result.put("totalCount", totalCount);
        result.put("totals", totals);
        return result;
    }
}