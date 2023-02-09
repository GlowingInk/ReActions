package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"SLOT", "HELP_SLOT"}) // My legacy typo...
public class HeldSlotFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String params) {
        Player player = context.getPlayer();
        return NumberUtils.asInteger(params, -1) == player.getInventory().getHeldItemSlot();
    }

    @Override
    public @NotNull String getName() {
        return "HELD_SLOT";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
