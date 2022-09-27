package me.fromgate.reactions.util.enums;

public enum DamageType {
    BLOCK,
    ENTITY,
    OTHER,
    ANY;

    public static DamageType getByName(String sourceStr) {
        if (sourceStr != null) {
            for (DamageType sourceType : values()) {
                if (sourceStr.equalsIgnoreCase(sourceType.name())) {
                    return sourceType;
                }
            }
        }
        return DamageType.ANY;
    }
}
