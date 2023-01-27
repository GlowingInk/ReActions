package me.fromgate.reactions.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@FunctionalInterface
public interface SafeSupplier<T> extends Supplier<T> {
    @Override
    @NotNull T get();

    static <T> @NotNull SafeSupplier<T> asSafe(@NotNull Supplier<T> supplier) {
        return () -> Objects.requireNonNull(supplier.get());
    }
}
