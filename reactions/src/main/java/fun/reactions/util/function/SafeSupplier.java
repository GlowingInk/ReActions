package fun.reactions.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@FunctionalInterface
public interface SafeSupplier<T> extends Supplier<T> {
    @Override
    @NotNull T get();

    static <T> @NotNull SafeSupplier<T> of(@NotNull Supplier<T> supplier) {
        return supplier instanceof SafeSupplier<T> safe
                ? safe
                : () -> Objects.requireNonNull(supplier.get());
    }
}
