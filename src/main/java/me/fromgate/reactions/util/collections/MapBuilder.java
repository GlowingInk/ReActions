package me.fromgate.reactions.util.collections;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K, V> {
    private final Map<K, V> map;

    public MapBuilder() {
        this.map = new HashMap<>();
    }

    public MapBuilder(K key, V value) {
        this.map = new HashMap<>();
        map.put(key,value);
    }

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public static <K, V> Map<K, V> single(K key, V value) {
        Map<K, V> map = new HashMap<>(1);
        map.put(key, value);
        return map;
    }

    public Map<K, V> build() {
        return map;
    }
}
