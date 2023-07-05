package fun.reactions.util.enums;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SafeEnum<E extends Enum<E>>(@Nullable E instance) {
    public boolean isValidFor(@Nullable E other) {
        return instance == null || instance == other;
    }

    public @NotNull String name() {
        return instance == null ? "ANY" : instance.name();
    }

    @Override
    public String toString() {
        return instance == null ? "ANY" : instance.toString();
    }
}
