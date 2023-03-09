package fun.reactions.util.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CaseInsensitiveMap<V> implements Map<String, V> {
    private final Map<String, String> realKeys;
    private final Map<String, V> realMap;

    private KeySet keySet;
    private Values values;
    private EntrySet entrySet;

    public CaseInsensitiveMap() {
        this(16);
    }

    public CaseInsensitiveMap(Map<String, ? extends V> copy) {
        this(copy.size());
        putAll(copy);
    }

    public CaseInsensitiveMap(int size) {
        realKeys = new HashMap<>(size);
        realMap = new LinkedHashMap<>(size);
    }

    protected String convert(String key) {
        return key.toLowerCase(Locale.ROOT);
    }

    @Override
    public int size() {
        return realKeys.size();
    }

    @Override
    public boolean isEmpty() {
        return realKeys.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String keyStr && realKeys.containsKey(convert(keyStr));
    }

    @Override
    public boolean containsValue(Object value) {
        return realMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return getOrDefault(key, null);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        if (!(key instanceof String strKey)) return defaultValue;
        String convertedKey = convert(strKey);
        String realKey = realKeys.get(convertedKey);
        return realKey == null ? defaultValue : realMap.get(realKey);
    }

    @Override
    public V put(String key, V value) {
        String convertedKey = convert(key);
        String oldKey = realKeys.put(convertedKey, key);
        if (oldKey == null) {
            realMap.put(key, value);
            return null;
        } else {
            V oldValue = realMap.remove(oldKey);
            realMap.put(key, value);
            return oldValue;
        }
    }

    @Override
    public V remove(Object key) {
        if (!(key instanceof String strKey)) return null;
        String convertedKey = convert(strKey);
        String realKey = realKeys.get(convertedKey);
        return realKey == null ? null : realMap.remove(realKey);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        realKeys.clear();
        realMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return keySet == null ? keySet = new KeySet() : keySet;
    }

    private class KeySet extends AbstractSet<String> {
        @Override
        public @NotNull Iterator<String> iterator() {
            return new KeyIterator();
        }

        private class KeyIterator implements Iterator<String> {
            private final Iterator<String> realMapIterator = realMap.keySet().iterator();
            private String current;

            @Override
            public boolean hasNext() {
                return realMapIterator.hasNext();
            }

            @Override
            public String next() {
                return current = realMapIterator.next();
            }

            @Override
            public void remove() {
                if (current == null) throw new IllegalStateException();
                realKeys.remove(convert(current));
                current = null;
            }
        }

        @Override
        public int size() {
            return CaseInsensitiveMap.this.size();
        }

        @Override
        public boolean remove(Object o) {
            return CaseInsensitiveMap.this.remove(o) != null;
        }

        @Override
        public void clear() {
            CaseInsensitiveMap.this.clear();
        }

        @Override
        public boolean contains(Object o) {
            return CaseInsensitiveMap.this.containsKey(o);
        }
    }

    @Override
    public Collection<V> values() {
        return values == null ? values = new Values() : values;
    }

    private class Values extends AbstractCollection<V> {
        private final Set<Entry<String, V>> realValues = realMap.entrySet();

        @Override
        public @NotNull Iterator<V> iterator() {
            return new ValuesIterator();
        }

        private class ValuesIterator implements Iterator<V> {
            private final Iterator<Entry<String, V>> realIterator = realValues.iterator();
            private Entry<String, V> current;

            @Override
            public boolean hasNext() {
                return realIterator.hasNext();
            }

            @Override
            public V next() {
                current = realIterator.next();
                return current.getValue();
            }

            @Override
            public void remove() {
                if (current == null) throw new IllegalStateException();
                realIterator.remove();
                realKeys.remove(convert(current.getKey()));
                current = null;
            }
        }

        @Override
        public int size() {
            return CaseInsensitiveMap.this.size();
        }

        @Override
        public boolean remove(Object o) {
            var iterator = realValues.iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (Objects.equals(entry, o)) {
                    iterator.remove();
                    realKeys.remove(convert(entry.getKey()));
                    return true;
                }
            }
            return false;
        }

        @Override
        public void clear() {
            CaseInsensitiveMap.this.clear();
        }
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return entrySet == null ? entrySet = new EntrySet() : entrySet;
    }

    private class EntrySet extends AbstractSet<Entry<String, V>> {
        private final Set<Entry<String, V>> realEntries = realMap.entrySet();

        @Override
        public @NotNull Iterator<Entry<String, V>> iterator() {
            return new EntryIterator();
        }

        private class EntryIterator implements Iterator<Entry<String, V>> {
            private final Iterator<Entry<String, V>> realIterator = realEntries.iterator();
            private Entry<String, V> current;

            @Override
            public boolean hasNext() {
                return realIterator.hasNext();
            }

            @Override
            public Entry<String, V> next() {
                return current = realIterator.next();
            }

            @Override
            public void remove() {
                if (current == null) throw new IllegalStateException();
                realIterator.remove();
                realKeys.remove(convert(current.getKey()));
                current = null;
            }
        }

        @Override
        public int size() {
            return CaseInsensitiveMap.this.size();
        }

        @Override
        public void clear() {
            CaseInsensitiveMap.this.clear();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(Object o) {
            if (realEntries.remove(o)) {
                realKeys.remove(convert(((Entry<String, ?>) o).getKey()));
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) return true;
        if (other instanceof Map<?, ?> map) {
            if (map.size() != size()) return false;
            for (Map.Entry<?,?> entry : map.entrySet()) {
                if (!Objects.equals(get(entry.getKey()), entry.getValue())) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return realKeys.keySet().hashCode() * realMap.values().hashCode();
    }

    @Override
    public String toString() {
        return realMap.toString();
    }
}
