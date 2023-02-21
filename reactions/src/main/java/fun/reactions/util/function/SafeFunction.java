package fun.reactions.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public interface SafeFunction<T, R> extends Function<T, R> {
    @Override
    @NotNull R apply(@NotNull T t);

    static <T, R> @NotNull SafeFunction<T, R> of(@NotNull Function<T, R> funct) {
        return funct instanceof SafeFunction<T, R> safe
                ? safe
                : (t) -> Objects.requireNonNull(funct.apply(t));
    }
}
