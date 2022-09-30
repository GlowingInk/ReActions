package me.fromgate.reactions.util.enums;

public enum ClickType {
    RIGHT,
    LEFT,
    ANY;

    public static ClickType getByName(String clickStr) {
        if (clickStr.equalsIgnoreCase("left")) return ClickType.LEFT;
        if (clickStr.equalsIgnoreCase("any")) return ClickType.ANY;
        return ClickType.RIGHT;
    }

    public boolean checkRight(boolean right) {
        return switch (this) {
            case ANY -> true;
            case LEFT -> !right;
            case RIGHT -> right;
        };
    }
}
