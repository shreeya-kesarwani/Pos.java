package com.pos.salesReport.integration.dto;

import com.pos.dao.*;
import com.pos.model.constants.OrderStatus;
import com.pos.model.form.SalesReportForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestEntities;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

public abstract class AbstractSalesReportDtoIT extends AbstractIntegrationTest {

    @Autowired protected ClientDao clientDao;
    @Autowired protected ProductDao productDao;
    @Autowired protected InventoryDao inventoryDao;
    @Autowired protected OrderDao orderDao;
    @Autowired protected OrderItemDao orderItemDao;

    protected SalesReportForm form(Integer clientId, LocalDate start, LocalDate end) {
        SalesReportForm f = new SalesReportForm();
        f.setClientId(clientId);
        f.setStartDate(start);
        f.setEndDate(end);
        return f;
    }

    protected void seedOneInvoicedSale(Integer clientId, String barcode) throws Exception {
        var product = TestEntities.newProduct(barcode, "P-" + barcode, clientId, 100.0, null);
        productDao.insert(product);

        inventoryDao.insert(TestEntities.newInventory(product.getId(), 50));

        var order = TestEntities.newOrder(OrderStatus.INVOICED, null);
        orderDao.insert(order);

        orderItemDao.insert(TestEntities.newOrderItem(order.getId(), product.getId(), 1, 10.0));

        flushAndClear();
    }
}