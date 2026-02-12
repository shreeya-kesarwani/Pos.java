package com.pos.flow;

import com.pos.api.InventoryApi;
import com.pos.api.OrderApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

@Component
@Transactional(rollbackFor = Exception.class)
public class OrderFlow {

    @Autowired private OrderApi orderApi;
    @Autowired private InventoryApi inventoryApi;
    @Autowired private ProductApi productApi;

    public Integer createOrder(List<OrderItem> items) throws ApiException {
        //todo- it should take list<orderitems> should happen after the logic
        Order order = orderApi.create();

        for (OrderItem item : items) {
            Integer productId = item.getProductId();
            Integer quantity = item.getQuantity();
            Double sellingPrice = item.getSellingPrice();
//todo - in api or in private method
            if (productId == null) {
                throw new ApiException(PRODUCT_NOT_FOUND.value());
            }
            if (quantity == null || quantity <= 0) {
                throw new ApiException(QUANTITY_MUST_BE_POSITIVE.value());
            }
            if (sellingPrice == null || sellingPrice < 0) {
                throw new ApiException(SELLING_PRICE_CANNOT_BE_NEGATIVE.value());
            }
            productApi.validateSellingPrice(productId, sellingPrice);
            inventoryApi.reduceInventory(productId, quantity);
            orderApi.addItem(order.getId(), productId, quantity, sellingPrice);
        }

        return order.getId();
    }

    public List<OrderItem> getOrderItems(Integer orderId) throws ApiException {
        orderApi.getCheck(orderId);
        List<OrderItem> items = orderApi.getItemsByOrderId(orderId);
        return (items == null) ? List.of() : items;
    }
}
