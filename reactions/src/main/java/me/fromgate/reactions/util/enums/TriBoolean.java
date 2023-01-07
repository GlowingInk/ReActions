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

    public @NotNull TriState adventure() {
        return triState;
    }

    public static @NotNull TriBoolean getByName(@Nullable String str) {
        if (str == null) return ANY;
        return switch (str.toUpperCase(Locale.ROOT)) {
            case "TRUE" -> TRUE;
            case "FALSE" -> FALSE;
            default -> ANY;
        };
    }
}
