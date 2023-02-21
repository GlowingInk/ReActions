package fun.reactions.util.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class FunctionalUtils {
    private FunctionalUtils() {}

    public static <T> @NotNull Supplier<T> asCaching(@NotNull Supplier<T> getter) {
        return getter instanceof CachingSupplier<T> caching ? caching : new CachingSupplier<>(getter);
    }

    public static <T> @NotNull SafeSupplier<T> asSafeCaching(@NotNull SafeSupplier<T> getter) {
        return getter instanceof SafeCachingSupplier<T> caching ? caching : new SafeCachingSupplier<>(getter);
    }

    private static class CachingSupplier<T> implements Supplier<T> {
        private final Supplier<T> origin;
        private T value;
        private boolean cached;

        public CachingSupplier(@NotNull Supplier<T> getter) {
            this.origin = getter;
        }

        @Override
        public @Nullable T get() {
            if (!cached) {
                value = origin.get();
                cached = true;
            }
            return value;
        }
    }

    private static class SafeCachingSupplier<T> implements SafeSupplier<T> {
        private final Supplier<T> origin;
        private T value;

        public SafeCachingSupplier(@NotNull SafeSupplier<T> getter) {
            this.origin = getter;
        }

        @Override
        public @NotNull T get() {
            return value == null ? (value = origin.get()) : value;
        }
    }
}
