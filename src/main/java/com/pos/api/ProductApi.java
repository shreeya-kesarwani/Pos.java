package com.pos.api;

import com.pos.dao.ProductDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pos.model.constants.ErrorMessages.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProductApi {

    @Autowired
    private ProductDao productDao;

    @Transactional(readOnly = true)
    public Product get(Integer id) {
        return productDao.selectById(id);
    }

    @Transactional(readOnly = true)
    public Product getCheck(Integer id) throws ApiException {
        Product product = get(id);
        if (product == null) {
            throw new ApiException(PRODUCT_NOT_FOUND.value() + ": " + id);
        }
        return product;
    }

    @Transactional(readOnly = true)
    public List<Product> getByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) return List.of();
        return productDao.selectByIds(ids);
    }

    @Transactional(readOnly = true)
    public List<Product> getByBarcodes(List<String> barcodes) {
        if (CollectionUtils.isEmpty(barcodes)) return List.of();
        return productDao.selectByBarcodes(barcodes);
    }

    @Transactional(readOnly = true)
    public List<Product> getCheckByBarcodes(List<String> barcodes) throws ApiException {
        if (CollectionUtils.isEmpty(barcodes)) {
            throw new ApiException(AT_LEAST_ONE_BARCODE_REQUIRED.value());
        }

        List<Product> found = productDao.selectByBarcodes(barcodes);
        Set<String> foundSet = toBarcodeSet(found);

        List<String> missing = findMissingBarcodes(barcodes, foundSet);
        if (!missing.isEmpty()) {
            throw new ApiException(PRODUCTS_NOT_FOUND_FOR_BARCODES.value() + ": " + missing);
        }

        return found;
    }

    @Transactional(readOnly = true)
    public Product getByBarcode(String barcode) {
        List<Product> list = productDao.selectByBarcodes(List.of(barcode));
        return list.isEmpty() ? null : list.get(0);
    }

    @Transactional(readOnly = true)
    public Product getCheckByBarcode(String barcode) throws ApiException {
        Product product = getByBarcode(barcode);
        if (product == null) {
            throw new ApiException(PRODUCT_NOT_FOUND_FOR_BARCODE.value() + ": " + barcode);
        }
        return product;
    }

    @Transactional(readOnly = true)
    public List<Integer> findProductIdsByBarcodeOrName(String barcode, String productName) {
        return productDao.findProductIdsByBarcodeOrName(barcode, productName);
    }

    public void add(Product product) throws ApiException {
        if (getByBarcode(product.getBarcode()) != null) {
            throw new ApiException(PRODUCT_BARCODE_ALREADY_EXISTS.value() + ": " + product.getBarcode());
        }
        productDao.insert(product);
    }

    public void addBulk(List<Product> products) throws ApiException {
        if (CollectionUtils.isEmpty(products)) return;

        List<String> barcodes = extractBarcodes(products);
        List<Product> existingProducts = getByBarcodes(barcodes);

        if (!existingProducts.isEmpty()) {
            throw new ApiException(
                    SOME_BARCODES_ALREADY_EXIST.value() + ": " + extractBarcodes(existingProducts)
            );
        }
        for (Product product : products) {
            productDao.insert(product);
        }
    }

    public void update(Integer productId, Product product) throws ApiException {
        Product existing = getCheck(productId);
        if (product.getBarcode() != null && !product.getBarcode().equals(existing.getBarcode())) {
            throw new ApiException("Barcode cannot be modified");
        }
        if (product.getClientId() != null && !product.getClientId().equals(existing.getClientId())) {
            throw new ApiException("Client cannot be modified");
        }
        existing.setName(product.getName());
        existing.setMrp(product.getMrp());
        existing.setImageUrl(product.getImageUrl());
        existing.setClientId(product.getClientId());
    }

    public void validateSellingPrice(Integer productId, Double sellingPrice) throws ApiException {
        Product product = getCheck(productId);
        if (sellingPrice > product.getMrp()) {
            throw new ApiException(
                    SELLING_PRICE_EXCEEDS_MRP.value() + " | productId=" + productId + ", mrp=" + product.getMrp() + ", sellingPrice=" + sellingPrice
            );
        }
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, String barcode, Integer clientId, int page, int pageSize) {
        return productDao.search(name, barcode, clientId, page, pageSize);
    }

    @Transactional(readOnly = true)
    public long getCount(String name, String barcode, Integer clientId) {
        return productDao.getCount(name, barcode, clientId);
    }

    // -------------------- Static helpers --------------------

    public static List<String> extractBarcodes(List<Product> products) {
        if (products == null || products.isEmpty()) return List.of();
        return products.stream()
                .map(Product::getBarcode)
                .toList();
    }

    public static Set<String> toBarcodeSet(List<Product> products) {
        if (products == null || products.isEmpty()) return Set.of();
        return products.stream()
                .map(Product::getBarcode)
                .collect(Collectors.toSet());
    }

    public static List<String> findMissingBarcodes(List<String> requested, Set<String> foundSet) {
        if (requested == null || requested.isEmpty()) return List.of();

        final Set<String> safeFoundSet = (foundSet == null) ? Set.of() : foundSet;

        return requested.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .filter(b -> !safeFoundSet.contains(b))
                .toList();
    }

    public static Map<String, Integer> toProductIdByBarcode(List<Product> products) {
        if (products == null || products.isEmpty()) return Map.of();

        return products.stream()
                .filter(p -> p.getBarcode() != null && p.getId() != null)
                .collect(Collectors.toMap(
                        Product::getBarcode,
                        Product::getId,
                        (a, b) -> a
                ));
    }


}
