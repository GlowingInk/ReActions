package me.fromgate.reactions.test;

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.logic.context.Environment;
import org.jetbrains.annotations.NotNull;

public class MockFlag implements Flag {
    private final boolean player;
    private final boolean result;
    private final String name;

    public MockFlag(boolean player, boolean result, String name) {
        this.player = player;
        this.result = result;
        this.name = name;
    }

    @Override
    public boolean requiresPlayer() {
        return player;
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
