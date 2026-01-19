package com.pos.service;

import com.pos.dao.ProductDao;
import com.pos.pojo.ProductPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductDao productDao;

    @Transactional(rollbackFor = ApiException.class)
    public void add(ProductPojo productPojo) throws ApiException {
        if (productDao.selectByBarcode(productPojo.getBarcode()) != null) {
            throw new ApiException("Product with this barcode already exists: " + productPojo.getBarcode());
        }
        productDao.insert(productPojo);
    }

    @Transactional(readOnly = true)
    public ProductPojo getCheck(Integer id) throws ApiException {
        ProductPojo productPojo = productDao.select(id, ProductPojo.class);
        if (productPojo == null) {
            throw new ApiException("Product with ID " + id + " does not exist");
        }
        return productPojo;
    }

    @Transactional(readOnly = true)
    public List<ProductPojo> getAll() {
        return productDao.selectAll(ProductPojo.class);
    }

    @Transactional(rollbackFor = ApiException.class)
    public void update(Integer id, ProductPojo productPojo) throws ApiException {
        ProductPojo pojo = getCheck(id);
        pojo.setName(productPojo.getName());
        pojo.setMrp(productPojo.getMrp());
        productDao.update(pojo);
    }

    @Transactional(readOnly = true)
    public ProductPojo getByBarcode(String barcode) throws ApiException {
        ProductPojo productPojo = productDao.selectByBarcode(barcode);
        if (productPojo == null) {
            throw new ApiException("Product with barcode " + barcode + " not found");
        }
        return productPojo;
    }

    @Transactional(readOnly = true)
    public List<ProductPojo> search(String name, String barcode) {
        return productDao.search(name, barcode);
    }
}