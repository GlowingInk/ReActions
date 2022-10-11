package me.fromgate.reactions.util.enums;

import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum HandType {
    MAIN,
    OFF,
    ANY;

    public static HandType getByName(@NotNull String clickStr) {
        /*
        Unfortunately, YAML may consider 'off' as 'false'
        http://yaml.org/type/bool.html
         */
        return switch (clickStr.toUpperCase(Locale.ROOT)) {
            case "MAIN" -> HandType.MAIN;
            case "OFF", "FALSE" -> HandType.OFF;
            default -> HandType.ANY;
        };
    }

    public boolean isAllowed(@NotNull EquipmentSlot slot) {
        return switch (this) {
            case MAIN -> slot == EquipmentSlot.HAND;
            case OFF -> slot == EquipmentSlot.OFF_HAND;
            case ANY -> true;
        };
    }
}
