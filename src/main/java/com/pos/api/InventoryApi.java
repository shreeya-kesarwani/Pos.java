package com.pos.api;

import com.pos.dao.InventoryDao;
import com.pos.exception.ApiException;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.pos.model.constants.ErrorMessages.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public Inventory get(Integer id) {
        return inventoryDao.selectById(id);
    }

    public Inventory getCheck(Integer id) throws ApiException {
        Inventory inventory = get(id);
        if (inventory == null) {
            throw new ApiException(INVENTORY_NOT_FOUND.value() + ": " + id);
        }
        return inventory;
    }

    public Inventory getByProductId(Integer productId) {
        return inventoryDao.selectByProductId(productId);
    }

    public Inventory getCheckByProductId(Integer productId) throws ApiException {
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new ApiException(INVENTORY_NOT_FOUND_FOR_PRODUCT.value() + ": " + productId);
        }
        return inventory;
    }

    public List<Inventory> findByProductIds(List<Integer> productIds, int page, int size) {
        if (CollectionUtils.isEmpty(productIds)) return List.of();
        return inventoryDao.findByProductIds(productIds, page, size);
    }

    public void add(List<Inventory> inventories) throws ApiException {
        if (CollectionUtils.isEmpty(inventories)) return;

        List<Integer> productIds = extractDistinctProductIdsFromInventories(inventories);
        List<Inventory> existingList = inventoryDao.selectByProductIds(productIds);
        Map<Integer, Inventory> existingByProductId = toInventoryByProductId(existingList);

        for (Inventory inventory : inventories) {
            Integer productId = inventory.getProductId();
            if (productId == null) {
                throw new ApiException(PRODUCT_NOT_FOUND.value());
            }
            Inventory existing = existingByProductId.get(productId);
            if (existing == null) {
                inventoryDao.insert(inventory);
            } else {
                existing.setQuantity(inventory.getQuantity());
            }
        }
    }

    public void reduceInventory(Integer productId, Integer quantity) throws ApiException {

        if (productId == null) throw new ApiException(PRODUCT_NOT_FOUND.value());
        if (quantity == null || quantity <= 0) {
            throw new ApiException(QUANTITY_MUST_BE_POSITIVE.value() + ": " + quantity);
        }

        Inventory inventory = getCheckByProductId(productId);
        if (inventory.getQuantity() < quantity) {
            throw new ApiException(
                    INSUFFICIENT_INVENTORY.value()
                            + " | productId=" + productId
                            + ", available=" + inventory.getQuantity()
                            + ", requested=" + quantity
            );
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
    }

    public Long getCountByProductIds(List<Integer> productIds) {
        if (CollectionUtils.isEmpty(productIds)) return 0L;
        return inventoryDao.getCountByProductIds(productIds);
    }

    public static List<String> extractBarcodes(List<InventoryForm> forms) {
        if (forms == null || forms.isEmpty()) return List.of();
        return forms.stream()
                .map(InventoryForm::getBarcode)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public static List<Integer> extractDistinctProductIds(List<Inventory> inventories) {
        if (inventories == null || inventories.isEmpty()) return List.of();
        return inventories.stream()
                .map(Inventory::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public static Map<Integer, Inventory> toInventoryByProductId(List<Inventory> existingList) {
        if (existingList == null || existingList.isEmpty()) return Map.of();

        return existingList.stream()
                .filter(i -> i.getProductId() != null)
                .collect(Collectors.toMap(
                        Inventory::getProductId,
                        i -> i,
                        (a, b) -> a
                ));
    }

    public static List<Integer> extractDistinctProductIdsFromInventories(List<Inventory> inventories) {
        if (inventories == null || inventories.isEmpty()) return List.of();

        return inventories.stream()
                .map(Inventory::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}