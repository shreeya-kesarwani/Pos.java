package com.pos.api;

import com.pos.dao.InventoryDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional(readOnly = true)
    public Inventory get(Integer id) {
        return inventoryDao.select(id, Inventory.class);
    }

    @Transactional(readOnly = true)
    public Inventory getCheck(Integer id) throws ApiException {
        Inventory inv = get(id);
        if (inv == null) throw new ApiException("Inventory record not found: " + id);
        return inv;
    }

    @Transactional(readOnly = true)
    public Inventory getByProductId(Integer productId) {
        return inventoryDao.selectByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Inventory getCheckByProductId(Integer productId) throws ApiException {
        Inventory inv = getByProductId(productId);
        if (inv == null) throw new ApiException("Inventory not found for productId: " + productId);
        return inv;
    }

    public void add(Inventory inventory) {
        inventoryDao.insert(inventory);
    }

    public void update(Integer id, Inventory inventory) throws ApiException {
        Inventory existing = getCheck(id);
        existing.setQuantity(inventory.getQuantity());
    }

    @Transactional(readOnly = true)
    public List<Inventory> search(String barcode, String productName, String clientName, int page, int size) {
        return inventoryDao.search(barcode, productName, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String barcode, String productName, String clientName) {
        return inventoryDao.getCount(barcode, productName, clientName);
    }

    public void allocate(Integer productId, Integer quantity) throws ApiException {
        if (quantity == null || quantity <= 0) {
            throw new ApiException("Quantity must be > 0");
        }

        Inventory inventory = getCheckByProductId(productId);

        if (inventory.getQuantity() < quantity) {
            throw new ApiException("Insufficient inventory for productId: " + productId);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
    }
}
