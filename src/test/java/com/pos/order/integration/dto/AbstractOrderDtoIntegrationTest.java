package com.pos.order.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dao.InventoryDao;
import com.pos.dao.ProductDao;
import com.pos.dto.OrderDto;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestEntities;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AbstractOrderDtoIntegrationTest extends AbstractIntegrationTest {

    @Autowired protected OrderDto orderDto;
    @Autowired protected ClientDao clientDao;
    @Autowired protected ProductDao productDao;
    @Autowired protected InventoryDao inventoryDao;

    protected OrderItemForm item(String barcode, Integer qty, Double sp) {
        OrderItemForm i = new OrderItemForm();
        i.setBarcode(barcode);
        i.setQuantity(qty);
        i.setSellingPrice(sp);
        return i;
    }

    protected OrderForm orderForm(OrderItemForm... items) {
        OrderForm f = new OrderForm();
        f.setItems(items == null ? null : List.of(items));
        return f;
    }

    protected Integer seedOrderWithOneItem(String barcode) throws Exception {
        var client = TestEntities.newClient("C-" + barcode, barcode + "@acme.com");
        clientDao.insert(client);

        var product = TestEntities.newProduct(barcode, "P-" + barcode, client.getId(), 100.0, null);
        productDao.insert(product);

        inventoryDao.insert(TestEntities.newInventory(product.getId(), 50));
        flushAndClear();

        Integer orderId = orderDto.create(orderForm(item(barcode, 1, 10.0)));
        flushAndClear();
        return orderId;
    }
}