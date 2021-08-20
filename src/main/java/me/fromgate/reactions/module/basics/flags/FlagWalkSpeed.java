package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.NumberUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 5/4/2017.
 */
public class FlagWalkSpeed extends Flag {
    @Override
    protected boolean check(@NotNull RaContext context, @NotNull Parameters params) {
        Player player = context.getPlayer();
        if (!NumberUtils.isInteger(params.toString())) return false;
        long walkSpeed = Math.round(player.getWalkSpeed() * 10);
        context.setVariable("walkspeed", Long.toString(walkSpeed));
        return walkSpeed >= Integer.parseInt(params.toString());
    }

    @Override
    public @NotNull String getName() {
        return null;
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
