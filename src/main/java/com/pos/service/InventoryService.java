package com.pos.service;

import com.pos.dao.InventoryDao;
import com.pos.pojo.InventoryPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class) // Pattern match: Class-level transactions
public class InventoryService {

    @Autowired
    private InventoryDao inventoryDao;

    public void add(InventoryPojo p) {
        // Simple insert as per minimalist requirement
        inventoryDao.insert(p);
    }

    // Matches ProductService update pattern: no dao.update(), just setting values
    public void update(Integer id, InventoryPojo p) throws ApiException {
        InventoryPojo existing = getCheck(id);
        existing.setQuantity(p.getQuantity());
    }

    @Transactional(readOnly = true)
    public InventoryPojo getCheck(Integer id) throws ApiException {
        InventoryPojo inventoryPojo = inventoryDao.select(id, InventoryPojo.class);
        if (inventoryPojo == null) {
            throw new ApiException(String.format("Inventory record with ID %d does not exist", id));
        }
        return inventoryPojo;
    }

    @Transactional(readOnly = true)
    public List<InventoryPojo> search(String barcode, String productName, String clientName) {
        return inventoryDao.search(barcode, productName, clientName);
    }

    // Matching the ProductService pattern of having a search-based count if needed
    @Transactional(readOnly = true)
    public Long getCount() {
        return inventoryDao.count(InventoryPojo.class);
    }
}