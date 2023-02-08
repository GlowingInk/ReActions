package me.fromgate.reactions.util.enums;

import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum HandType {
    MAIN,
    SECOND,
    ANY;

    public static HandType getByName(@NotNull String clickStr) {
        /*
        Unfortunately, YAML may consider 'off' as 'false'
        http://yaml.org/type/bool.html
         */
        return switch (clickStr.toUpperCase(Locale.ROOT)) {
            case "MAIN" -> HandType.MAIN;
            case "OFF", "FALSE", "SECOND" -> HandType.SECOND;
            default -> HandType.ANY;
        };
    }

    public boolean isValidFor(@NotNull EquipmentSlot slot) {
        return switch (this) {
            case MAIN -> slot == EquipmentSlot.HAND;
            case SECOND -> slot == EquipmentSlot.OFF_HAND;
            case ANY -> true;
        };
    }

    public boolean isValidFor(@NotNull HandType hand) {
        return this == ANY || this == hand;
    }
}
