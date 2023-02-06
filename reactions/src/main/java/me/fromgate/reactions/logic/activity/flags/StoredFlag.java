package me.fromgate.reactions.logic.activity.flags;

import me.fromgate.reactions.util.parameter.Parameterizable;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public class StoredFlag implements Parameterizable {
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

    public @NotNull Flag getActivity() {
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

    @Override
    public @NotNull Parameters asParameters() {
        return Parameters.singleton(flag.getName(), params);
    }
}
