package com.pos.api;

import com.pos.dao.InvoiceDao;
import com.pos.dao.OrderDao;
import com.pos.exception.ApiException;
import com.pos.model.data.OrderStatus;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.pojo.Invoice;
import com.pos.pojo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private InvoiceApi invoiceApi;

    @Autowired
    private InvoiceDao invoiceDao;

    public Order create() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        orderDao.insert(order);
        return order;
    }

    public Order getCheck(Integer orderId) throws ApiException {
        Order order = orderDao.select(orderId);
        if (order == null) {
            throw new ApiException("Order not found: " + orderId);
        }
        return order;
    }

    public void updateStatus(Integer orderId, OrderStatus status) throws ApiException {
        Order order = getCheck(orderId);
        order.setStatus(status);
        orderDao.update(order);

        // ✅ Generate invoice ONLY when order is completed
        if (status == OrderStatus.CREATED) {

            List<InvoiceItemForm> invoiceItems = buildInvoiceItems(orderId);

            InvoiceForm invoiceForm = new InvoiceForm();
            invoiceForm.setOrderId(orderId);
            invoiceForm.setItems(invoiceItems);

            InvoiceData invoiceData =
                    invoiceApi.generateInvoice(orderId, invoiceForm);


            Invoice invoice = new Invoice();
            invoice.setOrderId(invoiceData.getOrderId());
            invoice.setPath(invoiceData.getPdfPath());

            invoiceDao.insert(invoice);
        }
    }

    public List<Order> search(Integer id,
                              ZonedDateTime start,
                              ZonedDateTime end,
                              OrderStatus status) {
        return orderDao.search(id, start, end, status);
    }

    // 🔧 Helper: map order items → invoice items
    private List<InvoiceItemForm> buildInvoiceItems(Integer orderId) {
        // TODO:
        // 1. Fetch order items using orderId
        // 2. Map them to InvoiceItemForm
        // 3. Return the list
        return List.of(); // replace with real implementation
    }
}
