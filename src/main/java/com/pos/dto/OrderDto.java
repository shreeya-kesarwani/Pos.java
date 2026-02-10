package com.pos.dto;

import com.pos.api.OrderApi;
import com.pos.api.OrderItemApi;
import com.pos.api.ProductApi;
import com.pos.client.InvoiceClient;
import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.*;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.OrderForm;
import com.pos.pojo.*;
import com.pos.utils.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import static com.pos.model.constants.ErrorMessages.*;

@Component
public class OrderDto extends AbstractDto {

    private static final String INVOICE_DIR = "/tmp/invoices";
    @Autowired private OrderFlow orderFlow;
    @Autowired private OrderApi orderApi;
    @Autowired private OrderItemApi orderItemApi;
    @Autowired private ProductApi productApi;
    @Autowired private InvoiceClient invoiceClient;

    public Integer create(@Valid OrderForm form) throws ApiException {
        normalize(form);
        return orderFlow.createOrder(form);
    }

    public PaginatedResponse<OrderData> search(Integer id, ZonedDateTime start, ZonedDateTime end, String status, Integer pageNumber, Integer pageSize) throws ApiException {

        OrderStatus orderStatus = validateStatus(status);

        Map<String, Object> result = orderFlow.searchWithTotals(id, start, end, orderStatus, pageNumber, pageSize);
        List<Order> orders = (List<Order>) result.get("orders");
        Object totalCountObj = result.get("totalCount");
        long totalCount = (totalCountObj instanceof Long)
                ? (Long) totalCountObj
                : ((Number) totalCountObj).longValue();

        Map<Integer, Double> totals = (Map<Integer, Double>) result.get("totals");
        List<OrderData> data = new ArrayList<>();
        for (Order order : orders) {
            double totalAmount = totals.getOrDefault(order.getId(), 0.0);
            data.add(OrderConversion.toOrderData(order, totalAmount));
        }

        return PaginatedResponse.of(data, totalCount, pageNumber);
    }

    public List<OrderItemData> getItems(Integer orderId) throws ApiException {

        Map<String, Object> result = orderFlow.getOrderItemsWithProducts(orderId);
        List<OrderItem> items = (List<OrderItem>) result.get("items");
        if (items.isEmpty()) return List.of();

        Map<Integer, Product> productById = (Map<Integer, Product>) result.get("productById");
        List<OrderItemData> data = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productById.get(item.getProductId());
            if (product == null) {
                throw new ApiException(PRODUCT_NOT_FOUND.value() + ": productId=" + item.getProductId());
            }
            data.add(OrderConversion.toOrderItemData(item, product.getBarcode(), product.getName()));
        }
        return data;
    }

    public InvoiceData invoice(Integer orderId) throws ApiException {
        Order order = orderApi.getCheck(orderId);

        if (order.getStatus() == OrderStatus.INVOICED) {
            throw new ApiException(ORDER_ALREADY_INVOICED.value() + ": orderId=" + orderId);
        }
        InvoiceGenResult result = generateStoreAndAttachInvoice(orderId);
        return result.data;
    }

    public ResponseEntity<byte[]> downloadInvoiceResponse(Integer orderId) throws ApiException {
        byte[] pdf = downloadInvoice(orderId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + InvoicePathUtil.invoiceFileName(orderId))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    public byte[] downloadInvoice(Integer orderId) throws ApiException {
        Order order = orderApi.getCheck(orderId);

        if (order.getStatus() != OrderStatus.INVOICED) {
            throw new ApiException(INVOICE_NOT_GENERATED_YET.value() + ": orderId=" + orderId);
        }

        String path = order.getInvoicePath();
        boolean shouldRegenerate = (path == null || path.isBlank());
        if (!shouldRegenerate) {
            try {
                shouldRegenerate = !Files.exists(Paths.get(path));
            } catch (Exception ignored) {
                shouldRegenerate = true;
            }
        }
        if (shouldRegenerate) {
            return generateStoreAndAttachInvoice(orderId).pdfBytes;
        }
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            return generateStoreAndAttachInvoice(orderId).pdfBytes;
        }
    }

    private InvoiceGenResult generateStoreAndAttachInvoice(Integer orderId) throws ApiException {
        List<OrderItem> items = orderItemApi.getByOrderId(orderId);
        if (items.isEmpty()) {
            throw new ApiException(CANNOT_INVOICE_EMPTY_ORDER.value() + ": orderId=" + orderId);
        }

        Map<Integer, Product> productById = getProductByIdMap(items);
        InvoiceForm form = InvoiceConversion.toInvoiceForm(orderId, items, productById);
        validateForm(form);

        InvoiceData data = invoiceClient.generate(form);
        byte[] pdfBytes = InvoiceConversion.decodePdfBytes(data);

        storeInvoicePdfAndAttach(orderId, pdfBytes);
        return new InvoiceGenResult(data, pdfBytes);
    }

    private static final class InvoiceGenResult {
        private final InvoiceData data;
        private final byte[] pdfBytes;

        private InvoiceGenResult(InvoiceData data, byte[] pdfBytes) {
            this.data = data;
            this.pdfBytes = pdfBytes;
        }
    }

    private Map<Integer, Product> getProductByIdMap(List<OrderItem> items) {
        Set<Integer> productIds = items.stream()
                .map(OrderItem::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (productIds.isEmpty()) return Map.of();
        List<Product> products = productApi.getByIds(productIds.stream().toList());
        return CollectionIndexUtil.indexBy(products, Product::getId);
    }

    private OrderStatus validateStatus(String status) throws ApiException {
        if (status == null || status.trim().isEmpty()) return null;

        return EnumParseUtil.parseEnum(OrderStatus.class, status)
                .orElseThrow(() -> new ApiException(INVALID_STATUS.value() + ": " + status));
    }

    private void storeInvoicePdfAndAttach(Integer orderId, byte[] pdfBytes) throws ApiException {
        try {
            Path dir = InvoicePathUtil.invoiceDir(INVOICE_DIR);
            Files.createDirectories(dir);

            Path path = InvoicePathUtil.invoiceFilePath(INVOICE_DIR, orderId);
            Files.write(path, pdfBytes);

            orderApi.attachInvoice(orderId, path.toString());
        } catch (Exception e) {
            throw new ApiException(FAILED_TO_STORE_INVOICE_PDF.value() + ": orderId=" + orderId, e);
        }
    }
}
