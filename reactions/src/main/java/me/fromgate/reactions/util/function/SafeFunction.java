package me.fromgate.reactions.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface SafeFunction<T, R> extends Function<T, R> {
    @Override
    @NotNull R apply(@NotNull T t);
}
