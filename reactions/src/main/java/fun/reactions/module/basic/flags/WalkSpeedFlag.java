package fun.reactions.module.basic.flags;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

/**
 * @author MaxDikiy
 * @since 05/04/2017
 */
@Aliased.Names("WALKSPEED")
public class WalkSpeedFlag implements Flag, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        OptionalInt paramsOpt = NumberUtils.parseInteger(paramsStr, Is.NATURAL);
        if (paramsOpt.isEmpty()) return false;
        long walkSpeed = Math.round(player.getWalkSpeed() * 10); // TODO: Why?
        env.getVariables().set("walkspeed", Long.toString(walkSpeed));
        return walkSpeed >= paramsOpt.getAsInt();
    }

    @Override
    public @NotNull String getName() {
        return "WALK_SPEED";
    }

}
