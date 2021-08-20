package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class FlagCheckOnline extends Flag {
    @Override
    protected boolean check(@NotNull RaContext context, @NotNull Parameters params) {
        return Bukkit.getPlayerExact(params.toString()) != null;
    }

    @Override
    public @NotNull String getName() {
        return "CHECK_ONLINE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected boolean isParameterized() {
        return false;
    }
}
