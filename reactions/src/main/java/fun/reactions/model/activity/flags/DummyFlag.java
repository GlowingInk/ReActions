package fun.reactions.model.activity.flags;

import fun.reactions.model.environment.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class DummyFlag implements Flag {
    private final String name;

    public DummyFlag(@NotNull String name) {
        this.name = name.toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        return false;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }
}
