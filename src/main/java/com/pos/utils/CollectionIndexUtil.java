package com.pos.utils;

import java.util.*;
import java.util.function.Function;

public final class CollectionIndexUtil {

    public static <T, K> Map<K, T> indexBy(Collection<T> items, Function<T, K> keyFn) {
        if (items == null || items.isEmpty()) return Map.of();

        Map<K, T> map = new HashMap<>();
        for (T item : items) {
            if (item == null) continue;
            K key = keyFn.apply(item);
            if (key == null) continue;
            map.putIfAbsent(key, item);
        }
        return map;
    }
}
