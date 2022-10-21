package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class FlagCheckOnline implements Flag {
    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String params) {
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
