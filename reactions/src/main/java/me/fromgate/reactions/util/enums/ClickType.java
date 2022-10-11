package me.fromgate.reactions.util.enums;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ClickType {
    RIGHT,
    LEFT,
    ANY;

    public static ClickType getByName(@NotNull String clickStr) {
        return switch (clickStr.toUpperCase(Locale.ROOT)) {
            case "RIGHT" -> ClickType.RIGHT;
            case "LEFT" -> ClickType.LEFT;
            default -> ClickType.ANY;
        };
    }

    public boolean checkRight(boolean right) {
        return switch (this) {
            case RIGHT -> right;
            case LEFT -> !right;
            case ANY -> true;
        };
    }
}
