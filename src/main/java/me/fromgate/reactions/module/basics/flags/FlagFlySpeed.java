package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.NumberUtils;
import org.bukkit.entity.Player;

/**
 * Created by MaxDikiy on 5/4/2017.
 */
public class FlagFlySpeed implements OldFlag {
    @Override
    public boolean checkFlag(RaContext context, String param) {
        Player player = context.getPlayer();
        if (!NumberUtils.isInteger(param)) return false;
        long flySpeed = Math.round(player.getFlySpeed() * 10);
        context.setVariable("flyspeed", Integer.toString((int) flySpeed));
        return flySpeed >= Integer.parseInt(param);
    }
}
