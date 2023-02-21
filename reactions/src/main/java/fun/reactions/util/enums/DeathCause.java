package fun.reactions.util.enums;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum DeathCause {
    PVP,
    PVE,
    OTHER,
    ANY;

    public static DeathCause getByName(@NotNull String name) {
        return switch (name.toUpperCase(Locale.ROOT)) {
            case "PVP" -> DeathCause.PVP;
            case "PVE" -> DeathCause.PVE;
            case "OTHER" -> DeathCause.OTHER;
            default -> DeathCause.ANY;
        };
    }
}
