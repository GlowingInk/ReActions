package fun.reactions.model.activity.flags;

import fun.reactions.model.environment.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class InvalidFlag implements Flag {
    private final String name;

    public InvalidFlag(@NotNull String name) {
        this.name = name.toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean requiresPlayer() {
        return false;
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
