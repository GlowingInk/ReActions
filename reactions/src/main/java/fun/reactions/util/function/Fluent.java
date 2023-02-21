package fun.reactions.util.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class Fluent<T> {
    private final T obj;

    private Fluent(@NotNull T obj) {
        this.obj = obj;
    }

    public static <T> @NotNull T of(@NotNull T obj, @NotNull Consumer<T> funct) {
        funct.accept(obj);
        return obj;
    }

    public static <T> @NotNull Fluent<T> of(@NotNull T obj) {
        return new Fluent<>(obj);
    }

    public @NotNull Fluent<T> then(@NotNull Consumer<T> funct) {
        funct.accept(obj);
        return this;
    }

    public <F> @NotNull Fluent<F> then(@NotNull SafeFunction<T, F> funct) {
        return new Fluent<>(funct.apply(obj));
    }

    public <R> @Nullable R endUnsafe(@NotNull Function<T, R> funct) {
        return funct.apply(obj);
    }

    public <R> @NotNull R end(@NotNull SafeFunction<T, R> funct) {
        return funct.apply(obj);
    }

    public @NotNull T end(@NotNull Consumer<T> funct) {
        return then(funct).end();
    }

    public @NotNull T end() {
        return obj;
    }
}
