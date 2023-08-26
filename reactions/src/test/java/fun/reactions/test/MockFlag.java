package fun.reactions.test;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import org.jetbrains.annotations.NotNull;

public class MockFlag implements Flag {
    private final boolean result;
    private final String name;

    public MockFlag(boolean result, String name) {
        this.result = result;
        this.name = name;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        return result;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }
}
