package com.pos.flow;

import com.pos.api.InvoiceApi;
import com.pos.api.OrderApi;
import com.pos.api.OrderItemApi;
import com.pos.api.ProductApi;
import com.pos.dao.InvoiceDao;
import com.pos.exception.ApiException;
import com.pos.model.data.OrderStatus;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.pojo.Invoice;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Transactional(rollbackFor = Exception.class)
public class InvoiceFlow {

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InvoiceApi invoiceApi;

    @Autowired
    private InvoiceDao invoiceDao;

    public void generateInvoice(Integer orderId) throws ApiException {

        // 1. Validate order
        Order order = orderApi.getCheck(orderId);
        if (order.getStatus() == OrderStatus.INVOICED) {
            throw new ApiException("Invoice already generated");
        }

        // 2. Fetch order items
        List<OrderItem> orderItems = orderItemApi.getByOrderId(orderId);
        if (orderItems.isEmpty()) {
            throw new ApiException("Cannot generate invoice for empty order");
        }

        // 3. Build InvoiceForm
        InvoiceForm form = new InvoiceForm();
        form.setOrderId(orderId);

        List<InvoiceItemForm> invoiceItemForms = new ArrayList<>();

        for (OrderItem item : orderItems) {
            Product product = productApi.get(item.getProductId());

            InvoiceItemForm ii = new InvoiceItemForm();
            ii.setName(product.getName());
            ii.setQuantity(item.getQuantity());
            ii.setSellingPrice(item.getSellingPrice());

            invoiceItemForms.add(ii);
        }

        form.setItems(invoiceItemForms);

        // 4. Call invoice-app
        invoiceApi.generate(form);

        // 5. Persist invoice record
        Invoice invoice = new Invoice();
        invoice.setOrderId(orderId);
        invoice.setPath("invoice-" + orderId + ".pdf");
        invoiceDao.insert(invoice);

        // 6. Mark order INVOICED
        orderApi.updateStatus(orderId, OrderStatus.INVOICED);
    }

}
