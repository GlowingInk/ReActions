package me.fromgate.reactions.util.collections;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Basically this is a wrapper for HashMap<String, V> which allows to ignore case of the string key
 * For Map#get and Map#put works with O(1), while TreeMap<String, V>(String.CASE_INSENSITIVE_ORDER) is O(log(n))
 *
 * Should be used when keys are needed to save/proceed, otherwise HashMap<String, V> with String#toLowerCase
 * @param <V> Type of value
 * @author imDaniX
 */
public class CaseInsensitiveMap<V> implements Map<String, V> {
    private final Map<String, KeyedValue<V>> origin;
    private final KeySet keySet;
    private final ValueSet valueSet;
    private final EntrySet entrySet;

    public CaseInsensitiveMap() {
        this(16);
    }

    public CaseInsensitiveMap(@NotNull Map<String, V> copy) {
        this(copy.size());
        putAll(copy);
    }

    private CaseInsensitiveMap(int initSize) {
        origin = new HashMap<>(initSize);
        keySet = new KeySet();
        valueSet = new ValueSet();
        entrySet = new EntrySet();
    }

    @Override
    public int size() {
        return origin.size();
    }

    @Override
    public boolean isEmpty() {
        return origin.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return o instanceof String str && origin.containsKey(str.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean containsValue(Object o) {
        return origin.containsValue(new KeyedValue<>(null, o));
    }

    @Override
    public V get(Object o) {
        return o instanceof String str
                ? origin.getOrDefault(str.toLowerCase(Locale.ROOT), KeyedValue.empty()).getValue()
                : null;
    }

    @Override
    public V put(String s, V v) {
        KeyedValue<V> result = origin.put(s.toLowerCase(Locale.ROOT), new KeyedValue<>(s, v));
        return result == null
                ? null
                : result.getValue();
    }

    @Override
    public V remove(Object o) {
        KeyedValue<V> result = origin.remove(((String)o).toLowerCase(Locale.ROOT));
        return result == null
                ? null
                : result.getValue();
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> map) {
        map.forEach((k, v) -> origin.put(k.toLowerCase(Locale.ROOT), new KeyedValue<>(k, v)));
    }

    @Override
    public void clear() {
        origin.clear();
    }

    @Override
    public Set<String> keySet() {
        return keySet;
    }

    @Override
    public Collection<V> values() {
        return valueSet;
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return entrySet;
    }

    @SuppressWarnings("unchecked")
    private static class KeyedValue<V> implements Map.Entry<String, V> {
        private static final KeyedValue<?> EMPTY = new KeyedValue<>(null, null);

        private final String key;
        private V value;

        public KeyedValue(String key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return value == ((KeyedValue<V>) o).value || value.equals(((KeyedValue<V>) o).value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        public static <T> KeyedValue<T> empty() {
            return (KeyedValue<T>) EMPTY;
        }
    }

    private abstract class InternalSet<E> extends AbstractSet<E>{
        @Override
        public int size() {
            return CaseInsensitiveMap.this.size();
        }

        @Override
        public void clear() {
            CaseInsensitiveMap.this.clear();
        }
        // TODO etc?

        public abstract class InternalIterator implements Iterator<E> {
            final Iterator<KeyedValue<V>> internal;

            public InternalIterator() {
                this.internal = CaseInsensitiveMap.this.origin.values().iterator();
            }

            @Override
            public boolean hasNext() {
                return internal.hasNext();
            }

            @Override
            public void remove() {
                internal.remove();
            }
        }
    }

    private class KeySet extends InternalSet<String> {
        @Override
        public @NotNull Iterator<String> iterator() {
            return new KeyIterator();
        }

        private class KeyIterator extends InternalIterator {
            @Override
            public String next() {
                return internal.next().getKey();
            }
        }
    }

    private class ValueSet extends InternalSet<V> {
        @Override
        public @NotNull Iterator<V> iterator() {
            return new ValueIterator();
        }

        private class ValueIterator extends InternalIterator {
            @Override
            public V next() {
                return internal.next().getValue();
            }
        }
    }

    private class EntrySet extends InternalSet<Entry<String, V>> {
        @Override
        public @NotNull Iterator<Entry<String, V>> iterator() {
            return new EntrySet.EntryIterator();
        }

        private class EntryIterator extends InternalIterator {
            @Override
            public Entry<String, V> next() {
                return internal.next();
            }
        }
    }
}
