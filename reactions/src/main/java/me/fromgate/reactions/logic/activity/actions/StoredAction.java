package me.fromgate.reactions.logic.activity.actions;

import me.fromgate.reactions.util.parameter.Parameterizable;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public class StoredAction implements Parameterizable {
    private final Action action;
    private final String params;
    private final boolean placeholders;

    public StoredAction(@NotNull Action action, @NotNull String params) {
        this.action = action;
        this.params = params;
        this.placeholders = params.contains("%");
    }

    public @NotNull Action getAction() {
        return action;
    }

    public @NotNull String getParameters() {
        return params;
    }

    public boolean hasPlaceholders() {
        return placeholders;
    }

    @Override
    public @NotNull String toString() {
        return action.getName() + "=" + params;
    }

    @Override
    public @NotNull Parameters asParameters() {
        return Parameters.singleton(action.getName(), params);
    }
}