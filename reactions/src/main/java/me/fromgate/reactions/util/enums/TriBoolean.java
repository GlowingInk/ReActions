package me.fromgate.reactions.util.enums;

import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.BooleanSupplier;

public enum TriBoolean {
    TRUE(TriState.TRUE), FALSE(TriState.FALSE), ANY(TriState.NOT_SET);

    private final TriState triState;

    TriBoolean(TriState triState) {
        this.triState = triState;
    }

    public @Nullable Boolean asBoolean() {
        return triState.toBoolean();
    }

    public boolean asBoolean(boolean def) {
        return triState.toBooleanOrElse(def);
    }

    public boolean asBoolean(@NotNull BooleanSupplier def) {
        return triState.toBooleanOrElseGet(def);
    }

    public boolean isValidFor(boolean bool) {
        return switch (this) {
            case TRUE -> bool;
            case FALSE -> !bool;
            default -> true;
        };
    }

    public boolean isValidFor(@Nullable Boolean bool) {
        return switch (this) {
            case TRUE -> bool != null && bool;
            case FALSE -> bool != null && !bool;
            default -> true;
        };
    }

    public @NotNull TriState adventure() {
        return triState;
    }

    public static @NotNull TriBoolean of(@Nullable String str) {
        if (str == null) return ANY;
        return switch (str.toUpperCase(Locale.ROOT)) {
            case "TRUE", "ON", "ENABLE", "ENABLED" -> TRUE;
            case "FALSE", "OFF", "DISABLE", "DISABLED" -> FALSE;
            default -> ANY;
        };
    }

    public static @NotNull TriBoolean of(@Nullable Boolean bool) {
        if (bool == Boolean.TRUE) return TRUE;
        if (bool == Boolean.FALSE) return FALSE;
        return ANY;
    }

    public static @NotNull TriBoolean of(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    public static @NotNull TriBoolean of(@NotNull TriState triState) {
        return switch (triState) {
            case TRUE -> TRUE;
            case FALSE -> FALSE;
            default -> ANY;
        };
    }
}
