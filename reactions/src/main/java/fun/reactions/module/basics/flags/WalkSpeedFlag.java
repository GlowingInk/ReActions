package fun.reactions.module.basics.flags;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.NumberUtils.Is;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author MaxDikiy
 * @since 05/04/2017
 */
@Aliased.Names("WALKSPEED")
public class WalkSpeedFlag implements Flag, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        if (!NumberUtils.isNumber(paramsStr, Is.NATURAL)) return false;
        long walkSpeed = Math.round(player.getWalkSpeed() * 10); // TODO: Why?
        env.getVariables().set("walkspeed", Long.toString(walkSpeed));
        return walkSpeed >= Integer.parseInt(paramsStr);
    }

    @Override
    public @NotNull String getName() {
        return "WALK_SPEED";
    }

}
