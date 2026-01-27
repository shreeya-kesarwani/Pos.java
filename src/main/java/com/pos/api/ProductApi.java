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

    public Product get(Integer id) {
        return productDao.select(id, Product.class);
    }

    public Product getCheck(Integer id) throws ApiException {
        Product product = get(id);
        if (product == null) {
            throw new ApiException(String.format("Product with ID %d does not exist", id));
        }
        return product;
    }

    public Product getByBarcode(String barcode) {
        return productDao.selectByBarcode(barcode);
    }

    public Product getCheckByBarcode(String barcode) throws ApiException {
        Product product = getByBarcode(barcode);
        if (product == null) {
            throw new ApiException(String.format("Product with barcode [%s] not found", barcode));
        }
        return product;
    }

    public Integer getIdByBarcode(String barcode) throws ApiException {
        Product product = getCheckByBarcode(barcode);
        return product.getId();
    }

    public String getBarcodeById(Integer productId) throws ApiException {
        Product product = getCheck(productId);
        return product.getBarcode();
    }

    public String getNameById(Integer productId) throws ApiException {
        Product product = getCheck(productId);
        return product.getName();
    }

    public void add(Product product) throws ApiException {
        if (getByBarcode(product.getBarcode()) != null) {
            throw new ApiException(
                    String.format("Product with barcode [%s] already exists", product.getBarcode())
            );
        }
        productDao.insert(product);
    }

    public void update(String barcode, Product product) throws ApiException {
        Product existing = getCheckByBarcode(barcode);
        existing.setName(product.getName());
        existing.setMrp(product.getMrp());
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
