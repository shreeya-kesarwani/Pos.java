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

    public Inventory get(Integer id) {
        return inventoryDao.select(id, Inventory.class);
    }

    public Inventory getCheck(Integer id) throws ApiException {
        Inventory p = get(id);
        if (p == null) {
            throw new ApiException(String.format("Inventory record ID %d not found", id));
        }
        return p;
    }

    public Inventory getByProductId(Integer productId) {
        return inventoryDao.selectByProductId(productId);
    }

    public Inventory getCheckByProductId(Integer productId) throws ApiException {
        Inventory p = getByProductId(productId);
        if (p == null) {
            throw new ApiException(String.format("Inventory for Product ID %d not found", productId));
        }
        return p;
    }

    public void add(Inventory p) {
        inventoryDao.insert(p);
    }

    public void update(Integer id, Inventory p) throws ApiException {
        Inventory existing = getCheck(id);
        existing.setQuantity(p.getQuantity());
    }

    @Transactional(readOnly = true)
    public List<Inventory> search(String barcode, String productName, String clientName) {
        return inventoryDao.search(barcode, productName, clientName);
    }
}