package me.fromgate.reactions.util.collections;

import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;

public class Maps {
    public static <T> LinkedCaseInsensitiveMap<T> caseInsensitive(int size) {
        return new LinkedCaseInsensitiveMap<>(size);
    }

    public static <T> LinkedCaseInsensitiveMap<T> caseInsensitive() {
        return new LinkedCaseInsensitiveMap<>();
    }

    public static <T> LinkedCaseInsensitiveMap<T> caseInsensitive(Map<String, T> origin) {
        return new LinkedCaseInsensitiveMap<>(origin);
    }

    public static class Builder<K, V> {
        private final Map<K, V> map;

        public Builder() {
            this.map = new HashMap<>();
        }

        public Builder(K key, V value) {
            this.map = new HashMap<>();
            map.put(key,value);
        }

        public Builder<K, V> put(K key, V value) {
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
}
