package fun.reactions.util.collections;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class CollectionUtils { private CollectionUtils() { }
    public static final Hash.Strategy<String> CASE_INSENSITIVE_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(@Nullable String str) {
            if (str == null) return -1;
            final int length = str.length();
            int result = 0;
            for (int i = 0; i < length; i++) {
                result = 31 * result + Character.toLowerCase(str.charAt(i));
            }
            return result;
        }

        @Override
        public boolean equals(@Nullable String left, @Nullable String right) {
            return left == null
                    ? right == null
                    : left.equalsIgnoreCase(right);
        }
    };

    public static <T> @NotNull Map<String, T> caseInsensitiveLinkedMap() {
        return new Object2ObjectLinkedOpenCustomHashMap<>(CASE_INSENSITIVE_STRATEGY);
    }

    public static <T> @NotNull Map<String, T> caseInsensitiveLinkedMap(int initSize) {
        return new Object2ObjectLinkedOpenCustomHashMap<>(initSize, CASE_INSENSITIVE_STRATEGY);
    }

    public static <T> @NotNull Map<String, T> caseInsensitiveLinkedMap(Map<String, T> origin) {
        return new Object2ObjectLinkedOpenCustomHashMap<>(origin, CASE_INSENSITIVE_STRATEGY);
    }

    public static <T> @NotNull Map<String, T> caseInsensitiveMap() {
        return new Object2ObjectOpenCustomHashMap<>(CASE_INSENSITIVE_STRATEGY);
    }

    public static <T> @NotNull Map<String, T> caseInsensitiveMap(int initSize) {
        return new Object2ObjectOpenCustomHashMap<>(initSize, CASE_INSENSITIVE_STRATEGY);
    }

    public static <T> @NotNull Map<String, T> caseInsensitiveMap(Map<String, T> origin) {
        return new Object2ObjectOpenCustomHashMap<>(origin, CASE_INSENSITIVE_STRATEGY);
    }

    public static <T> @NotNull List<T> emptyOnNull(@Nullable T @Nullable [] arr) {
        return arr == null ? List.of() : Arrays.asList(arr);
    }
}
