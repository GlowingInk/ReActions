package me.fromgate.reactions.util.enums;

import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum HandType {
    MAIN,
    OFF,
    ANY;

    public static HandType getByName(String clickStr) {
        /*
        Unfortunately, YAML may consider 'off' as 'false'
        http://yaml.org/type/bool.html
         */
        return switch (clickStr.toLowerCase(Locale.ROOT)) {
            case "off", "false" -> HandType.OFF;
            case "any" -> HandType.ANY;
            default -> HandType.MAIN;
        };
    }

    public boolean isAllowed(@NotNull EquipmentSlot slot) {
        return switch (this) {
            case ANY -> true;
            case MAIN -> slot == EquipmentSlot.HAND;
            case OFF -> slot == EquipmentSlot.OFF_HAND;
        };
    }
}
