package com.pos.dto;

import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.OrderForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import com.pos.utils.OrderConversion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderDto extends AbstractDto {

    @Autowired
    private OrderFlow orderFlow;

    public Integer create(@Valid OrderForm form) throws ApiException {
        normalize(form);
        return orderFlow.createOrder(form);
    }

    public PaginatedResponse<OrderData> search(Integer id, ZonedDateTime start, ZonedDateTime end, String status, Integer pageNumber, Integer pageSize) throws ApiException {

        OrderStatus orderStatus = parseStatus(status);

        List<Order> orders = orderFlow.search(id, start, end, orderStatus, pageNumber, pageSize);
        Long totalCount = orderFlow.getCount(id, start, end, orderStatus);

        List<OrderData> data = new ArrayList<>();
        for (Order order : orders) {
            Double totalAmount = orderFlow.calculateTotalAmount(order.getId());
            data.add(OrderConversion.toOrderData(order, totalAmount));
        }
        return PaginatedResponse.of(data, totalCount, pageNumber);
    }

    public List<OrderItemData> getItems(Integer orderId) throws ApiException {

        List<OrderItem> items = orderFlow.getOrderItems(orderId);
        if (items.isEmpty()) return List.of();

        Set<Integer> productIds = items.stream()
                .map(OrderItem::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Product> products = orderFlow.getProductsByIds(productIds);
        Map<Integer, Product> productById = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        List<OrderItemData> data = new ArrayList<>();
        for (OrderItem item : items) {
            Product p = productById.get(item.getProductId());
            if (p == null) throw new ApiException("Product not found: " + item.getProductId());
            data.add(OrderConversion.toOrderItemData(item, p.getBarcode(), p.getName()));
        }

        return data;
    }

    private OrderStatus parseStatus(String status) throws ApiException {
        if (status == null || status.trim().isEmpty()) return null;
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid status: " + status + ". Allowed: CREATED, INVOICED");
        }
    }
}
