package me.fromgate.reactions.util.enums;

public enum HandType {
    MAIN,
    OFF,
    ANY;

    public static HandType getByName(String clickStr) {
        /*
        If we parse this Yaml ...
        hand: off
        ... we get this:
        'hand': false
        In order to use the string 'off' as a key, you need to wrap it with quotes:
        hand: 'off'
        http://yaml.org/type/bool.html
         */
        if (clickStr.equalsIgnoreCase("off") || clickStr.equalsIgnoreCase("false")) return HandType.OFF;
        if (clickStr.equalsIgnoreCase("any")) return HandType.ANY;
        return HandType.MAIN;
    }

    public boolean checkMain(boolean isMain) {
        return switch (this) {
            case ANY -> true;
            case MAIN -> isMain;
            case OFF -> !isMain;
        };
    }

    public boolean checkOff(boolean isOff) {
        return switch (this) {
            case ANY -> true;
            case MAIN -> !isOff;
            case OFF -> isOff;
        };
    }
}
