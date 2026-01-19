package com.pos.service;

import com.pos.dao.InventoryDao;
import com.pos.pojo.InventoryPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional(rollbackFor = ApiException.class)
    public void update(Integer productId, Integer quantity) throws ApiException {
        InventoryPojo inventoryPojo = inventoryDao.select(productId, InventoryPojo.class);

        if (inventoryPojo == null) {
            throw new ApiException("Inventory record missing for Product ID: " + productId);
        }
        inventoryPojo.setQuantity(quantity);
        inventoryDao.update(inventoryPojo);
    }

    @Transactional(readOnly = true)
    public List<InventoryPojo> getAll() {
        return inventoryDao.selectAll(InventoryPojo.class);
    }

    @Transactional(readOnly = true)
    public InventoryPojo get(Integer productId) throws ApiException {
        InventoryPojo inventoryPojo = inventoryDao.select(productId, InventoryPojo.class);
        if (inventoryPojo == null) {
            throw new ApiException("Inventory record missing for Product ID: " + productId);
        }
        return inventoryPojo;
    }

    public Integer getQuantitySafe(Integer id) {
        try {
            return get(id).getQuantity();
        } catch (Exception exception) {
            return 0;
        }
    }
}