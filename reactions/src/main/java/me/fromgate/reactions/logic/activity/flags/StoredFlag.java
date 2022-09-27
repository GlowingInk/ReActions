package me.fromgate.reactions.logic.activity.flags;

import org.jetbrains.annotations.NotNull;

public class StoredFlag {

    private final Flag flag;
    private final String params;
    private final boolean inverted;
    private final boolean placeholders;

    public StoredFlag(@NotNull Flag flag, @NotNull String params, boolean inverted) {
        this.flag = flag;
        this.params = params;
        this.inverted = inverted;
        this.placeholders = params.contains("%");
    }

    public @NotNull Flag getFlag() {
        return flag;
    }

    public @NotNull String getParameters() {
        return params;
    }

    public boolean isInverted() {
        return inverted;
    }

    public boolean hasPlaceholders() {
        return placeholders;
    }

    @Override
    public @NotNull String toString() {
        return (inverted ? "!" : "") + flag.getName() + "=" + params;
    }
}
