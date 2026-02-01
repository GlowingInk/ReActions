package fun.reactions.model.activity.flags;

import fun.reactions.model.environment.Environment;
import org.jetbrains.annotations.NotNull;

public record InvalidFlag(@NotNull String name) implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        return false;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }
}
