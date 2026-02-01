package fun.reactions.model.activity.actions;

import fun.reactions.model.environment.Environment;
import org.jetbrains.annotations.NotNull;

public record InvalidAction(@NotNull String name) implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        return false;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }
}
