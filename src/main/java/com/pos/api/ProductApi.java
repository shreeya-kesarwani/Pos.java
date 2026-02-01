package com.pos.api;

import com.pos.dao.ProductDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ProductApi {

    @Autowired
    private ProductDao productDao;

    // ---------- Basic getters ----------

    public Product get(Integer id) throws ApiException {
        if (id == null) {
            throw new ApiException("Product id cannot be null");
        }
        return productDao.select(id, Product.class);
    }

    public Product getCheck(Integer id) throws ApiException {
        Product product = get(id);
        if (product == null) {
            throw new ApiException(String.format("Product with ID %d does not exist", id));
        }
        return product;
    }

    public Product getByBarcode(String barcode) throws ApiException {
        String b = normalizeBarcode(barcode);
        if (b == null) {
            throw new ApiException("Barcode cannot be empty");
        }

        // ProductDao does not have selectByBarcode in your latest paste,
        // so we fetch via IN query.
        List<Product> list = productDao.selectByBarcodes(List.of(b));
        return list.isEmpty() ? null : list.get(0);
    }

    public Product getCheckByBarcode(String barcode) throws ApiException {
        String b = normalizeBarcode(barcode);
        Product product = getByBarcode(b);
        if (product == null) {
            throw new ApiException(String.format("Product with barcode [%s] not found", b));
        }
        return product;
    }

    public Integer getIdByBarcode(String barcode) throws ApiException {
        return getCheckByBarcode(barcode).getId();
    }

    public String getBarcodeById(Integer productId) throws ApiException {
        return getCheck(productId).getBarcode();
    }

    public String getNameById(Integer productId) throws ApiException {
        return getCheck(productId).getName();
    }

    // ---------- Bulk fetchers (fix for your build errors) ----------

    @Transactional(readOnly = true)
    public List<Product> getByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return productDao.selectByIds(ids);
    }

    @Transactional(readOnly = true)
    public List<Product> getByBarcodes(List<String> barcodes) {
        List<String> normalized = normalizeBarcodes(barcodes);
        if (normalized.isEmpty()) return List.of();
        return productDao.selectByBarcodes(normalized);
    }

    /**
     * Bulk "check" method: throws if ANY barcode does not exist.
     * Useful for OrderFlow, etc.
     */
    @Transactional(readOnly = true)
    public List<Product> getCheckByBarcodes(List<String> barcodes) throws ApiException {
        List<String> normalized = normalizeBarcodes(barcodes);
        if (normalized.isEmpty()) {
            throw new ApiException("Barcodes cannot be empty");
        }

        List<Product> found = productDao.selectByBarcodes(normalized);

        Set<String> foundSet = found.stream()
                .map(p -> p.getBarcode() == null ? null : p.getBarcode().trim())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<String> missing = normalized.stream()
                .filter(b -> !foundSet.contains(b))
                .toList();

        if (!missing.isEmpty()) {
            throw new ApiException("Products not found for barcodes: " + missing);
        }

        return found;
    }

    // ---------- Create / Update ----------

    public void add(Product product) throws ApiException {
        if (product == null) {
            throw new ApiException("Product cannot be null");
        }

        String barcode = normalizeBarcode(product.getBarcode());
        if (barcode == null) {
            throw new ApiException("Product barcode cannot be empty");
        }
        product.setBarcode(barcode);

        if (getByBarcode(barcode) != null) {
            throw new ApiException(String.format("Product with barcode [%s] already exists", barcode));
        }

        productDao.insert(product);
    }

    /**
     * Used in bulk upload when duplicates are checked elsewhere (or intentionally allowed).
     */
    public void addWithoutBarcodeCheck(Product product) throws ApiException {
        if (product == null) throw new ApiException("Product cannot be null");

        String barcode = normalizeBarcode(product.getBarcode());
        if (barcode == null) throw new ApiException("Product barcode cannot be empty");

        product.setBarcode(barcode);
        productDao.insert(product);
    }

    public void update(String barcode, Product product) throws ApiException {
        if (product == null) {
            throw new ApiException("Product cannot be null");
        }

        Product existing = getCheckByBarcode(barcode);

        // name validation
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new ApiException("Product name cannot be empty");
        }

        // mrp validation
        if (product.getMrp() == null) {
            throw new ApiException("Product mrp cannot be null");
        }
        if (product.getMrp() < 0) {
            throw new ApiException("Product mrp cannot be negative");
        }

        // barcode update + uniqueness check
        String newBarcode = normalizeBarcode(product.getBarcode());
        if (newBarcode == null) {
            throw new ApiException("Product barcode cannot be empty");
        }

        if (!newBarcode.equals(existing.getBarcode())) {
            Product other = getByBarcode(newBarcode);
            if (other != null && !other.getId().equals(existing.getId())) {
                throw new ApiException(String.format("Product with barcode [%s] already exists", newBarcode));
            }
            existing.setBarcode(newBarcode);
        }

        existing.setName(product.getName().trim());
        existing.setMrp(product.getMrp());
        existing.setImageUrl(product.getImageUrl() == null ? null : product.getImageUrl().trim());
        // No explicit DAO update needed if JPA dirty checking is on and the entity is managed.
        // If your DAO requires explicit update, call productDao.update(existing).
    }

    // ---------- Search / Count ----------

    @Transactional(readOnly = true)
    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        return productDao.search(name, barcode, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String name, String barcode, String clientName) {
        return productDao.getCount(name, barcode, clientName);
    }

    // ---------- Business rules ----------

    public void validateSellingPrice(Integer productId, Double sellingPrice) throws ApiException {
        if (productId == null) {
            throw new ApiException("Product id cannot be null");
        }
        if (sellingPrice == null) {
            throw new ApiException("Selling price cannot be null");
        }
        if (sellingPrice < 0) {
            throw new ApiException("Selling price cannot be negative");
        }

        Product product = getCheck(productId);

        if (product.getMrp() == null) {
            throw new ApiException("MRP is not set for product: " + productId);
        }

        if (sellingPrice > product.getMrp()) {
            throw new ApiException(
                    "Selling price cannot be greater than MRP for product: " + product.getName()
            );
        }
    }

    // ---------- Helpers ----------

    private static String normalizeBarcode(String barcode) {
        if (barcode == null) return null;
        String b = barcode.trim();
        return b.isEmpty() ? null : b;
    }

    private static List<String> normalizeBarcodes(List<String> barcodes) {
        if (barcodes == null) return List.of();

        return barcodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }
}
