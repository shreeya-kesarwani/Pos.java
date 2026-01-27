package com.pos.dto;

import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.utils.OrderConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderDto extends AbstractDto {

    @Autowired
    private OrderFlow orderFlow;

    // -------------------------------------------------
    // CREATE ORDER
    // -------------------------------------------------
    public Integer create(OrderForm form) throws ApiException {

        validateForm(form);

        if (form.getItems() == null || form.getItems().isEmpty()) {
            throw new ApiException("Order must contain at least one item");
        }

        List<OrderItem> items = new ArrayList<>();

        for (OrderItemForm f : form.getItems()) {

            normalize(f);
            validateForm(f);
            validatePositive(f.getSellingPrice(), "sellingPrice");

            if (f.getQuantity() <= 0) {
                throw new ApiException("quantity must be greater than zero");
            }

            // ✅ DTO ONLY maps form → pojo
            OrderItem item = OrderConversion.toOrderItemPojo(f, null);
            items.add(item);
        }

        // ✅ ALL business logic happens in Flow
        return orderFlow.createOrder(items);
    }

    // -------------------------------------------------
    // SEARCH (PAGINATED)
    // -------------------------------------------------
    public PaginatedResponse<OrderData> search(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            String status,
            int page,
            int size
    ) throws ApiException {

        List<Order> orders =
                orderFlow.search(id, start, end, status, page, size);

        Long totalCount =
                orderFlow.getCount(id, start, end, status);

        List<OrderData> data = new ArrayList<>();

        for (Order order : orders) {

            Double totalAmount =
                    orderFlow.calculateTotalAmount(order.getId());

            OrderData d =
                    OrderConversion.toOrderData(order, totalAmount);

            data.add(d);
        }

        return PaginatedResponse.of(data, totalCount, page);
    }

    // -------------------------------------------------
    // ORDER ITEMS
    // -------------------------------------------------
    public List<OrderItemData> getItems(Integer orderId)
            throws ApiException {

        // ✅ DTO does NOT call ProductApi
        return orderFlow.getOrderItemData(orderId);
    }
}
