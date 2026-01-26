package com.pos.dto;

import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
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

    @Autowired
    private ProductApi productApi;

    /**
     * Create Order
     */
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

            // ✅ helper usage
            Integer productId = productApi.getIdByBarcode(f.getBarcode());

            OrderItem item = OrderConversion.toOrderItemPojo(f, productId);
            items.add(item);
        }

        return orderFlow.createOrder(items);
    }


    public List<OrderData> search(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            String status
    ) throws ApiException {

        List<Order> orders = orderFlow.search(id, start, end, status);
        List<OrderData> data = new ArrayList<>();

        for (Order order : orders) {

            // fetch items for this order
            List<OrderItem> items = orderFlow.getItems(order.getId());

            // compute total using helper
            Double totalAmount = OrderConversion.calculateTotalAmount(items);

            // convert to data
            OrderData d = OrderConversion.toOrderData(order, totalAmount);
            data.add(d);
        }

        return data;
    }


    public List<OrderItemData> getItems(Integer orderId) throws ApiException {

        List<OrderItem> items = orderFlow.getItems(orderId);
        List<OrderItemData> data = new ArrayList<>();

        for (OrderItem item : items) {

            Integer productId = item.getProductId();

            String barcode = productApi.getBarcodeById(productId);
            String productName = productApi.getNameById(productId);

            OrderItemData d = OrderConversion.toOrderItemData(
                    item,
                    barcode,
                    productName
            );

            data.add(d);
        }

        return data;
    }



    public void markInvoiced(Integer orderId) throws ApiException {
        orderFlow.markOrderInvoiced(orderId);
    }
}
