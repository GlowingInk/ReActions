package me.fromgate.reactions.logic.activity.actions;

import org.jetbrains.annotations.NotNull;

public class StoredAction {

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
}