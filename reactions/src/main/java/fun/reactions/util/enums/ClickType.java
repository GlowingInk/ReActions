package fun.reactions.util.enums;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum ClickType {
    RIGHT,
    LEFT,
    ANY;

    public static @NotNull ClickType getByName(@Nullable String clickStr) {
        if (clickStr == null) return ANY;
        return switch (clickStr.toUpperCase(Locale.ROOT)) {
            case "RIGHT" -> RIGHT;
            case "LEFT" -> LEFT;
            default -> ANY;
        };
    }

    public boolean isValidFor(@NotNull ClickType click) {
        return this == ANY || this == click;
    }

    @Deprecated
    public boolean checkRight(boolean right) {
        return switch (this) {
            case RIGHT -> right;
            case LEFT -> !right;
            case ANY -> true;
        };
    }
}
