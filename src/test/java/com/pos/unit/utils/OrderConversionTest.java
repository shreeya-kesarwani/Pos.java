package com.pos.unit.utils;

import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import com.pos.utils.OrderConversion;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OrderConversionTest {

    private static Order order(Integer id, OrderStatus status, ZonedDateTime createdAt) {
        Order o = new Order();
        o.setId(id);
        o.setStatus(status);
        o.setCreatedAt(createdAt);
        return o;
    }

    private static OrderItem item(Integer id, Integer productId, Integer qty, Double price) {
        OrderItem oi = new OrderItem();
        oi.setId(id);
        oi.setProductId(productId);
        oi.setQuantity(qty);
        oi.setSellingPrice(price);
        return oi;
    }

    private static Product product(Integer id, String barcode, String name) {
        Product p = new Product();
        p.setId(id);
        p.setBarcode(barcode);
        p.setName(name);
        return p;
    }

    @Test
    void toOrderData_shouldMapFields() {
        ZonedDateTime now = ZonedDateTime.now();
        Order o = order(10, OrderStatus.CREATED, now);

        OrderData d = OrderConversion.toOrderData(o, 123.45);

        assertEquals(10, d.getId());
        assertEquals(now, d.getCreatedAt());
        assertEquals("CREATED", d.getStatus());
        assertEquals(123.45, d.getTotalAmount());
    }

    @Test
    void toOrderItemData_shouldMapFields() {
        OrderItem oi = item(5, 99, 2, 10.0);

        OrderItemData d = OrderConversion.toOrderItemData(oi, "bc1", "p1");

        assertEquals(5, d.getId());
        assertEquals("bc1", d.getBarcode());
        assertEquals("p1", d.getProductName());
        assertEquals(2, d.getQuantity());
        assertEquals(10.0, d.getSellingPrice());
    }

    @Test
    void toOrderItemDataList_simple_shouldMapOnlyIdQtyPrice() {
        OrderItem i1 = item(1, 101, 2, 10.0);
        OrderItem i2 = item(2, 102, 3, 5.0);

        List<OrderItemData> out = OrderConversion.toOrderItemDataList(Arrays.asList(i1, i2));

        assertEquals(2, out.size());

        assertEquals(1, out.get(0).getId());
        assertEquals(2, out.get(0).getQuantity());
        assertEquals(10.0, out.get(0).getSellingPrice());
        assertNull(out.get(0).getBarcode());      // not set in this overload
        assertNull(out.get(0).getProductName());  // not set in this overload

        assertEquals(2, out.get(1).getId());
        assertEquals(3, out.get(1).getQuantity());
        assertEquals(5.0, out.get(1).getSellingPrice());
    }

    @Test
    void toOrderItemPojo_shouldMapFormAndProductId() {
        OrderItemForm f = new OrderItemForm();
        f.setQuantity(7);
        f.setSellingPrice(12.5);

        OrderItem oi = OrderConversion.toOrderItemPojo(f, 555);

        assertEquals(555, oi.getProductId());
        assertEquals(7, oi.getQuantity());
        assertEquals(12.5, oi.getSellingPrice());
    }

    @Test
    void toOrderDataWithTotal_shouldUseOrderMathUtilTotal() {
        ZonedDateTime now = ZonedDateTime.now();
        Order o = order(1, OrderStatus.CREATED, now);

        List<OrderItem> items = Arrays.asList(
                item(1, 10, 2, 10.0),  // 20
                item(2, 11, 1, 5.5)    // 5.5
        );

        OrderData d = OrderConversion.toOrderDataWithTotal(o, items);

        assertEquals(1, d.getId());
        assertEquals("CREATED", d.getStatus());
        assertEquals(25.5, d.getTotalAmount(), 0.0001);
    }

    @Test
    void toOrderItemDataList_withProductMap_shouldReturnEmpty_whenItemsNull() throws Exception {
        List<OrderItemData> out = OrderConversion.toOrderItemDataList(null, Map.of());
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void toOrderItemDataList_withProductMap_shouldReturnEmpty_whenItemsEmpty() throws Exception {
        List<OrderItemData> out = OrderConversion.toOrderItemDataList(List.of(), Map.of());
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void toOrderItemDataList_withProductMap_shouldMapBarcodeAndName() throws Exception {
        OrderItem i1 = item(1, 101, 2, 10.0);
        OrderItem i2 = item(2, 102, 3, 5.0);

        Map<Integer, Product> map = new HashMap<>();
        map.put(101, product(101, "bc101", "p101"));
        map.put(102, product(102, "bc102", "p102"));

        List<OrderItemData> out = OrderConversion.toOrderItemDataList(Arrays.asList(i1, i2), map);

        assertEquals(2, out.size());

        assertEquals(1, out.get(0).getId());
        assertEquals("bc101", out.get(0).getBarcode());
        assertEquals("p101", out.get(0).getProductName());
        assertEquals(2, out.get(0).getQuantity());
        assertEquals(10.0, out.get(0).getSellingPrice());

        assertEquals(2, out.get(1).getId());
        assertEquals("bc102", out.get(1).getBarcode());
        assertEquals("p102", out.get(1).getProductName());
        assertEquals(3, out.get(1).getQuantity());
        assertEquals(5.0, out.get(1).getSellingPrice());
    }

    @Test
    void toOrderItemDataList_withProductMap_shouldThrow_whenProductMissing() {
        OrderItem i1 = item(1, 999, 2, 10.0);

        ApiException ex = assertThrows(ApiException.class,
                () -> OrderConversion.toOrderItemDataList(List.of(i1), Map.of()));

        assertTrue(ex.getMessage().contains("Product not found"));
        assertTrue(ex.getMessage().contains("productId=999"));
    }
}
