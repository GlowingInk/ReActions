package fun.reactions.test;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import org.jetbrains.annotations.NotNull;

public record MockFlag(boolean result, String name) implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        return result;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }
}
