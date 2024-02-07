package fun.reactions.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class RichOptional<R, T> {
    private final R key;
    private final Optional<T> value;

    private RichOptional(@NotNull R key, @NotNull Optional<T> value) {
        this.key = key;
        this.value = value;
    }

    public static <R, T> RichOptional<R, T> of(@NotNull R key, @NotNull T value) {
        return new RichOptional<>(key, Optional.of(value));
    }

    public static <R, T> RichOptional<R, T> ofNullable(@NotNull R key, @Nullable T value) {
        return new RichOptional<>(key, Optional.ofNullable(value));
    }

    public T get() {
        return value.get();
    }

    public boolean isPresent() {
        return value.isPresent();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public @NotNull R key() {
        return key;
    }

    public @NotNull Optional<T> opt() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RichOptional<?, ?>) obj;
        return Objects.equals(this.key, that.key) &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "RichOptional[" +
                "key=" + key + ", " +
                "value=" + value + ']';
    }

}
