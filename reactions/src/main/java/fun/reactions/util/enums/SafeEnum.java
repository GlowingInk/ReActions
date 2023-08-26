package fun.reactions.util.enums;

import fun.reactions.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record SafeEnum<E extends Enum<E>>(@Nullable E instance) {
    public SafeEnum(@NotNull String name, @NotNull Class<E> clazz) {
        this(Utils.getEnum(clazz, name));
    }

    public boolean isValidFor(@Nullable E other) {
        return instance == null || instance == other;
    }

    public @NotNull Optional<E> optional() {
        return Optional.ofNullable(instance);
    }

    public @NotNull String name() {
        return instance == null ? "ANY" : instance.name();
    }

    @Override
    public String toString() {
        return instance == null ? "ANY" : instance.toString();
    }
}
