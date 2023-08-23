package fun.reactions.model.activity.actions;

import fun.reactions.model.environment.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class InvalidAction implements Action {
    private final String name;

    public InvalidAction(@NotNull String name) {
        this.name = name.toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        return false;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }
}
