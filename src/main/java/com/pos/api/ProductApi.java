package com.pos.api;

import com.pos.dao.ProductDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
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
        Set<String> foundSet = found.stream()
                .map(Product::getBarcode)
                .collect(Collectors.toSet());

        List<String> missing = barcodes.stream()
                .distinct()
                .filter(b -> !foundSet.contains(b))
                .toList();

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
        List<Integer> ids = productDao.findProductIdsByBarcodeOrName(barcode, productName);
        System.out.println("Api" + ids);
        return ids;
    }

    public void add(Product product) throws ApiException {
        if (getByBarcode(product.getBarcode()) != null) {
            throw new ApiException(PRODUCT_BARCODE_ALREADY_EXISTS.value() + ": " + product.getBarcode());
        }
        productDao.insert(product);
    }

    public void addBulk(List<Product> products) throws ApiException {
        if (CollectionUtils.isEmpty(products)) return;

        Set<String> barcodes = products.stream()
                .map(Product::getBarcode)
                .collect(Collectors.toSet());

        List<Product> existing = getByBarcodes(new ArrayList<>(barcodes));
        if (!existing.isEmpty()) {
            throw new ApiException(
                    SOME_BARCODES_ALREADY_EXIST.value() + ": " + existing.stream().map(Product::getBarcode).toList()
            );
        }

        for (Product product : products) {
            productDao.insert(product);
        }
    }

    public void update(String barcode, Product product) throws ApiException {
        Product existing = getCheckByBarcode(barcode);
        //todo - idempotency
        if (!product.getBarcode().equals(existing.getBarcode())) {
            Product other = getByBarcode(product.getBarcode());
            if (other != null && !other.getId().equals(existing.getId())) {
                throw new ApiException(PRODUCT_BARCODE_ALREADY_EXISTS.value() + ": " + product.getBarcode());
            }
            existing.setBarcode(product.getBarcode());
        }
        existing.setName(product.getName());
        existing.setMrp(product.getMrp());
        existing.setImageUrl(product.getImageUrl());
        existing.setClientId(product.getClientId());
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        return productDao.search(name, barcode, clientName, page, size);
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
    public Long getCount(String name, String barcode, String clientName) {
        return productDao.getCount(name, barcode, clientName);
    }

}
