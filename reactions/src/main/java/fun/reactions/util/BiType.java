package fun.reactions.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public class BiType<A, B> {
    private final Converter<A, B> converter;

    private A a;
    private B b;

    private BiType(@Nullable A a, @Nullable B b, @Nullable Converter<A, B> converter) {
        this.a = a;
        this.b = b;
        this.converter = converter;
    }

    @Contract(pure = true)
    public static <A, B> @NotNull BiType<A, B> ofA(@NotNull A a, @NotNull Converter<A, B> converter) {
        return new BiType<>(a, null, converter);
    }

    @Contract(pure = true)
    public static <A, B> @NotNull BiType<A, B> ofB(@NotNull B b, @NotNull Converter<A, B> converter) {
        return new BiType<>(null, b, converter);
    }

    @Contract(pure = true)
    public static <A, B> BiType<A, B> ofBoth(@NotNull A a, @NotNull B b) {
        return new BiType<>(a, b, null);
    }

    @Contract(pure = true, value = "null, null, _ -> fail")
    public static <A, B> BiType<A, B> of(@Nullable A a, @Nullable B b, @NotNull Converter<A, B> converter) {
        if (a == null && b == null) throw new IllegalArgumentException("Both A and B cannot be null.");
        return new BiType<>(a, b, converter);
    }

    public @NotNull A a() {
        return a == null ? (a = requireNonNull(converter).convertBtoA(b)) : a;
    }

    public @NotNull B b() {
        return b == null ? (b = requireNonNull(converter).convertAtoB(a)) : b;
    }

    public interface Converter<A, B> {
        @NotNull B convertAtoB(@NotNull A a);
        @NotNull A convertBtoA(@NotNull B b);
    }
}
