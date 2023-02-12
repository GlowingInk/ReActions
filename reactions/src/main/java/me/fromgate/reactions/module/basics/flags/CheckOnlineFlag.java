package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.naming.Aliased;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Aliased.Names("IS_ONLINE")
public class CheckOnlineFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String params) {
        UUID id = Utils.parseUniqueId(params);
        return (id == null ? Bukkit.getPlayerExact(params) : Bukkit.getPlayer(id)) != null;
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
