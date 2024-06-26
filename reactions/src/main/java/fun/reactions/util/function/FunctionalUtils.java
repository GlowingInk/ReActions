package fun.reactions.util.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FunctionalUtils {
    private FunctionalUtils() {}

    public static <T> @NotNull Consumer<T> asConsumer(@NotNull Function<T, ?> function) {
        return function::apply;
    }

    public static <T> @NotNull Supplier<T> asCaching(@NotNull Supplier<T> getter) {
        return getter instanceof CachingSupplier<T> caching ? caching : new CachingSupplier<>(getter);
    }

    public static <T> @NotNull SafeSupplier<T> asSafeCaching(@NotNull SafeSupplier<T> getter) {
        return getter instanceof SafeCachingSupplier<T> caching ? caching : new SafeCachingSupplier<>(getter);
    }

    public static <T, R> @NotNull Function<T, R> asCaching(@NotNull Function<T, R> getter) {
        return getter instanceof CachingFunction<T, R> caching ? caching : new CachingFunction<>(getter);
    }

    public static <T, R> @NotNull SafeFunction<T, R> asSafeCaching(@NotNull SafeFunction<T, R> getter) {
        return getter instanceof SafeCachingFunction<T, R> caching ? caching : new SafeCachingFunction<>(getter);
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

    private static class CachingFunction<T, R> implements Function<T, R> {
        private final Function<T, R> origin;
        private R value;
        private boolean cached;

        public CachingFunction(@NotNull Function<T, R> getter) {
            this.origin = getter;
        }

        @Override
        public @Nullable R apply(T t) {
            if (!cached) {
                value = origin.apply(t);
                cached = true;
            }
            return value;
        }
    }

    private static class SafeCachingFunction<T, R> implements SafeFunction<T, R> {
        private final SafeFunction<T, R> origin;
        private R value;

        public SafeCachingFunction(@NotNull SafeFunction<T, R> getter) {
            this.origin = getter;
        }

        @Override
        public @NotNull R apply(@NotNull T t) {
            return value == null ? (value = origin.apply(t)) : value;
        }
    }
}
