package com.pos.api;

import com.pos.dao.ProductDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ProductApi {

    @Autowired
    private ProductDao productDao;

    public Product get(Integer id) throws ApiException {
        if (id == null) {
            throw new ApiException("Product id cannot be null");
        }
        return productDao.select(id, Product.class);
    }

    public Product getCheck(Integer id) throws ApiException {
        Product product = get(id); // will throw if id is null
        if (product == null) {
            throw new ApiException(String.format("Product with ID %d does not exist", id));
        }
        return product;
    }

    public Product getByBarcode(String barcode) throws ApiException {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be empty");
        }
        return productDao.selectByBarcode(barcode.trim());
    }

    public Product getCheckByBarcode(String barcode) throws ApiException {
        Product product = getByBarcode(barcode); // validates barcode
        if (product == null) {
            throw new ApiException(String.format("Product with barcode [%s] not found", barcode));
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

    public void add(Product product) throws ApiException {
        if (product == null) {
            throw new ApiException("Product cannot be null");
        }
        if (product.getBarcode() == null || product.getBarcode().trim().isEmpty()) {
            throw new ApiException("Product barcode cannot be empty");
        }

        String barcode = product.getBarcode();
        if (getByBarcode(barcode) != null) {
            throw new ApiException(String.format("Product with barcode [%s] already exists", barcode));
        }

        productDao.insert(product);
    }

    public void update(String barcode, Product product) throws ApiException {
        if (product == null) {
            throw new ApiException("Product cannot be null");
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new ApiException("Product name cannot be empty");
        }
        if (product.getMrp() == null) {
            throw new ApiException("Product mrp cannot be null");
        }

        Product existing = getCheckByBarcode(barcode);

        String newBarcode = product.getBarcode();
        if (newBarcode == null || newBarcode.isEmpty()) {
            throw new ApiException("Product barcode cannot be empty");
        }

        if (!newBarcode.equals(existing.getBarcode())) {
            Product other = getByBarcode(newBarcode);
            if (other != null && !other.getId().equals(existing.getId())) {
                throw new ApiException(String.format("Product with barcode [%s] already exists", newBarcode));
            }
            existing.setBarcode(newBarcode);
        }
        existing.setName(product.getName());
        existing.setMrp(product.getMrp());
        existing.setImageUrl(product.getImageUrl() == null ? null : product.getImageUrl());
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        return productDao.search(name, barcode, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String name, String barcode, String clientName) {
        return productDao.getCount(name, barcode, clientName);
    }
}
