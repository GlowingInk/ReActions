package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.NumberUtils.Is;
import me.fromgate.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("SLOT")
public class FlagHeldSlot implements Flag {
    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String params) {
        Player player = context.getPlayer();
        return NumberUtils.isNumber(params, Is.NATURAL) && player.getInventory().getHeldItemSlot() == Integer.parseInt(params);
    }

    @Override
    public @NotNull String getName() {
        return "HELP_SLOT";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
