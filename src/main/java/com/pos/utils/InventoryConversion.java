package com.pos.utils;

import com.pos.model.data.InventoryData;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InventoryConversion {

    public static Inventory convertFormToPojo(InventoryForm form) {
        Inventory p = new Inventory();
        p.setQuantity(form.getQuantity());
        return p;
    }

    public static InventoryData convertPojoToData(Inventory p, String barcode, String productName) {
        InventoryData d = new InventoryData();
        d.setQuantity(p.getQuantity());
        d.setBarcode(barcode);
        d.setProductName(productName);
        return d;
    }

    public static List<Inventory> convertFormsToPojos(List<InventoryForm> forms) {
        if (forms == null || forms.isEmpty()) return List.of();
        return forms.stream()
                .map(InventoryConversion::convertFormToPojo)
                .toList();
    }

    public static List<InventoryData> toDataList(List<Inventory> inventories, List<Product> products) {
        if (inventories == null || inventories.isEmpty()) return List.of();
        if (products == null || products.isEmpty()) return List.of();

        Map<Integer, Product> productMap = products.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        return inventories.stream()
                .map(inv -> {
                    Product p = productMap.get(inv.getProductId());
                    if (p == null) return null;
                    return convertPojoToData(inv, p.getBarcode(), p.getName());
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public static List<Inventory> convertFormsToPojos(
            List<InventoryForm> forms,
            Map<String, Integer> productIdByBarcode
    ) {
        if (forms == null || forms.isEmpty()) return List.of();

        return forms.stream()
                .map(f -> {
                    Inventory inv = convertFormToPojo(f);
                    inv.setProductId(productIdByBarcode.get(f.getBarcode()));
                    return inv;
                })
                .toList();
    }

}