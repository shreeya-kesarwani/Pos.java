package com.pos.flow;

import com.pos.api.InventoryApi;
import com.pos.api.OrderApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

@Component
@Transactional(rollbackFor = Exception.class)
public class OrderFlow {

    @Autowired private OrderApi orderApi;
    @Autowired private InventoryApi inventoryApi;
    @Autowired private ProductApi productApi;

    public Integer createOrder(List<OrderItem> items) throws ApiException {

        if (CollectionUtils.isEmpty(items)) {
            throw new ApiException(NO_ORDER_ITEMS_FOUND.value());
        }
        for (OrderItem item : items) {
            if (item.getProductId() == null) {
                throw new ApiException(PRODUCT_NOT_FOUND.value());
            }
            productApi.validateSellingPrice(item.getProductId(), item.getSellingPrice());
            inventoryApi.reduceInventory(item.getProductId(), item.getQuantity());
        }
        return orderApi.create(items);
    }


    public List<OrderItem> getOrderItems(Integer orderId) throws ApiException {
        orderApi.getCheck(orderId);
        List<OrderItem> items = orderApi.getItemsByOrderId(orderId);
        return (items == null) ? List.of() : items;
    }
}
