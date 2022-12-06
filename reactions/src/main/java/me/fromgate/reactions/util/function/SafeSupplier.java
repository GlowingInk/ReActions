package me.fromgate.reactions.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@FunctionalInterface
public interface SafeSupplier<T> extends Supplier<T> {
    @Override
    @NotNull T get();
}
