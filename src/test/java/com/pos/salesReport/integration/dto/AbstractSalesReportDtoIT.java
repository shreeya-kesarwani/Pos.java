package com.pos.salesReport.integration.dto;

import com.pos.model.constants.OrderStatus;
import com.pos.model.form.SalesReportForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestEntities;
import com.pos.setup.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

public abstract class AbstractSalesReportDtoIT extends AbstractIntegrationTest {

    @Autowired protected TestFactory factory;

    protected SalesReportForm form(Integer clientId, LocalDate start, LocalDate end) {
        SalesReportForm f = new SalesReportForm();
        f.setClientId(clientId);
        f.setStartDate(start);
        f.setEndDate(end);
        return f;
    }

    protected void seedOneInvoicedSale(Integer clientId, String barcode) throws Exception {
        var product = factory.createProduct(barcode, "P-" + barcode, clientId, 100.0, null);
        factory.createInventory(product.getId(), 50);

        var order = factory.createOrder(OrderStatus.INVOICED, null);
        factory.createOrderItems(order.getId(),
                List.of(TestEntities.orderItem(order.getId(), product.getId(), 1, 10.0))
        );
        flushAndClear();
    }
}