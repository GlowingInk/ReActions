package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 5/4/2017.
 */
@Aliased.Names("WALKSPEED")
public class FlagWalkSpeed implements Flag {
    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String params) {
        Player player = context.getPlayer();
        if (!NumberUtils.isPositiveInt(params)) return false;
        long walkSpeed = Math.round(player.getWalkSpeed() * 10); // TODO: Why?
        context.setVariable("walkspeed", Long.toString(walkSpeed));
        return walkSpeed >= Integer.parseInt(params);
    }

    @Override
    public @NotNull String getName() {
        return "WALK_SPEED";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
