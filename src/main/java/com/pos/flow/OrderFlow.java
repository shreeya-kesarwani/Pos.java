package com.pos.flow;

import com.pos.api.InventoryApi;
import com.pos.api.OrderApi;
import com.pos.api.OrderItemApi;
import com.pos.exception.ApiException;
import com.pos.model.data.OrderStatus;
import com.pos.pojo.Inventory;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@Transactional(rollbackFor = Exception.class)
public class OrderFlow {

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private InventoryApi inventoryApi;

    /**
     * Create order using POJOs only
     */
    public Integer createOrder(List<OrderItem> items) throws ApiException {

        // 1. Create order
        Order order = orderApi.create();

        // 2. Process items
        for (OrderItem item : items) {

            item.setOrderId(order.getId());

            Inventory inventory =
                    inventoryApi.getByProductId(item.getProductId());

            if (inventory == null || inventory.getQuantity() < item.getQuantity()) {
                throw new ApiException("Insufficient inventory for productId: " + item.getProductId());
            }

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryApi.update(inventory.getId(), inventory);

            orderItemApi.add(item);
        }

        return order.getId();
    }


    public void markOrderInvoiced(Integer orderId) throws ApiException {
        orderApi.updateStatus(orderId, OrderStatus.INVOICED);
    }

    public List<Order> search(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            String status
    ) throws ApiException {

        OrderStatus orderStatus = null;
        if (status != null) {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        }

        return orderApi.search(id, start, end, orderStatus);
    }


    public List<OrderItem> getItems(Integer orderId) {
        return orderItemApi.getByOrderId(orderId);
    }

}
