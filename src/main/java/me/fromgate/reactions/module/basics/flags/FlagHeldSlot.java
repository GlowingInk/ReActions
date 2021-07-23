package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.NumberUtils;
import org.bukkit.entity.Player;

public class FlagHeldSlot implements OldFlag {
    @Override
    public boolean checkFlag(RaContext context, String param) {
        Player player = context.getPlayer();
        return NumberUtils.INT_POSITIVE.matcher(param).matches() && player.getInventory().getHeldItemSlot() == Integer.parseInt(param);
    }
}
