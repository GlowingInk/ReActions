package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.logic.context.Environment;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class FlagCheckOnline implements Flag {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String params) {
        return Bukkit.getPlayerExact(params) != null;
    }

    @Override
    public @NotNull String getName() {
        return "CHECK_ONLINE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
