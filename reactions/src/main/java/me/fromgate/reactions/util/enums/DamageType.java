package me.fromgate.reactions.util.enums;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum DamageType {
    BLOCK,
    ENTITY,
    OTHER,
    ANY;

    public static DamageType getByName(@NotNull String sourceStr) {
        return switch (sourceStr.toUpperCase(Locale.ROOT)) {
            case "BLOCK" -> DamageType.BLOCK;
            case "ENTITY" -> DamageType.ENTITY;
            case "OTHER" -> DamageType.OTHER;
            default -> DamageType.ANY;
        };
    }
}
