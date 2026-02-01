package com.pos.integration.report;

import com.pos.api.DaySalesApi;
import com.pos.api.OrderApi;
import com.pos.dao.ClientDao;
import com.pos.dao.DaySalesDao;
import com.pos.dto.OrderDto;
import com.pos.dto.ProductDto;
import com.pos.integration.AbstractMySqlIntegrationTest;
import com.pos.model.constants.OrderStatus;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Client;
import com.pos.pojo.DaySales;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DaySalesSchedulerIT extends AbstractMySqlIntegrationTest {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    @Autowired private DaySalesApi daySalesApi;
    @Autowired private DaySalesDao daySalesDao;

    @Autowired private OrderDto orderDto;
    @Autowired private OrderApi orderApi;

    @Autowired private ProductDto productDto;
    @Autowired private ClientDao clientDao;

    private static ZonedDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay(BUSINESS_ZONE);
    }

    private static ZonedDateTime endOfDay(LocalDate date) {
        // inclusive end-of-day (works nicely with SQL BETWEEN)
        return date.plusDays(1).atStartOfDay(BUSINESS_ZONE).minusNanos(1);
    }

    private void insertClient(String name, String email) {
        Client c = new Client();
        c.setName(name);
        c.setEmail(email);
        clientDao.insert(c);
    }

    private void addProduct(String clientName, String barcode, String name, double mrp) throws Exception {
        ProductForm f = new ProductForm();
        f.setClientName(clientName);
        f.setBarcode(barcode);
        f.setName(name);
        f.setMrp(mrp);
        f.setImageUrl(null);
        productDto.add(f);
    }

    private static OrderItemForm item(String barcode, int quantity, double sellingPrice) {
        OrderItemForm i = new OrderItemForm();
        i.setBarcode(barcode);
        i.setQuantity(quantity);
        i.setSellingPrice(sellingPrice);
        return i;
    }

    private Integer createOrderWithItems(List<OrderItemForm> items) throws Exception {
        OrderForm form = new OrderForm();
        form.setItems(items);
        return orderDto.create(form);
    }

    @Test
    void calculateAndStore_shouldInsertOrUpdateRow_withCorrectAggregates_forToday() throws Exception {
        // Arrange
        insertClient("ABC", "abc@test.com");
        addProduct("ABC", "DS1", "Pen", 10.0);
        addProduct("ABC", "DS2", "Pencil", 5.0);

        Integer orderId = createOrderWithItems(List.of(
                item("DS1", 2, 9.0),  // revenue 18
                item("DS2", 3, 4.0)   // revenue 12
        ));

        // IMPORTANT: your SQL counts only INVOICED orders and uses DATE(updated_at)
        orderApi.updateStatus(orderId, OrderStatus.INVOICED);

        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        ZonedDateTime todayStart = startOfDay(today);

        // Act
        daySalesApi.calculateAndStore(todayStart);

        // Assert
        List<DaySales> rows = daySalesDao.selectBetweenDates(startOfDay(today), endOfDay(today));
        assertEquals(1, rows.size());

        DaySales d = rows.get(0);

        // If DaySales stores a ZonedDateTime (as your error suggests), compare by LocalDate.
        assertEquals(today, d.getDate().withZoneSameInstant(BUSINESS_ZONE).toLocalDate());

        assertEquals(1, d.getInvoicedOrdersCount());
        assertEquals(5, d.getInvoicedItemsCount());

        // totalRevenue appears to be double (your "cannot call methods on double" error)
        assertEquals(30.0, d.getTotalRevenue(), 0.0001);
    }

    @Test
    void calculateAndStore_shouldBeIdempotent_mergeShouldNotCreateDuplicates() throws Exception {
        // Arrange
        insertClient("ABC", "abc@test.com");
        addProduct("ABC", "DS3", "Marker", 20.0);

        Integer orderId = createOrderWithItems(List.of(
                item("DS3", 2, 18.0) // revenue 36
        ));
        orderApi.updateStatus(orderId, OrderStatus.INVOICED);

        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        ZonedDateTime todayStart = startOfDay(today);

        // Act: run twice
        daySalesApi.calculateAndStore(todayStart);
        daySalesApi.calculateAndStore(todayStart);

        // Assert: still exactly 1 row for the date
        List<DaySales> rows = daySalesDao.selectBetweenDates(startOfDay(today), endOfDay(today));
        assertEquals(1, rows.size());

        DaySales d = rows.get(0);
        assertEquals(today, d.getDate().withZoneSameInstant(BUSINESS_ZONE).toLocalDate());
        assertEquals(1, d.getInvoicedOrdersCount());
        assertEquals(2, d.getInvoicedItemsCount());
        assertEquals(36.0, d.getTotalRevenue(), 0.0001);
    }

    @Test
    void calculateAndStore_shouldStoreZeros_whenNoInvoicedOrdersThatDay() throws Exception {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        ZonedDateTime todayStart = startOfDay(today);

        // Act
        daySalesApi.calculateAndStore(todayStart);

        // Assert
        List<DaySales> rows = daySalesDao.selectBetweenDates(startOfDay(today), endOfDay(today));
        assertEquals(1, rows.size());

        DaySales d = rows.get(0);
        assertEquals(today, d.getDate().withZoneSameInstant(BUSINESS_ZONE).toLocalDate());
        assertEquals(0, d.getInvoicedOrdersCount());
        assertEquals(0, d.getInvoicedItemsCount());
        assertEquals(0.0, d.getTotalRevenue(), 0.0001);
    }
}
