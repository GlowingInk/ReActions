package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.math.MathUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliases("SLOT")
public class FlagHeldSlot implements Flag {
    @Override
    public boolean check(@NotNull RaContext context, @NotNull String params) {
        Player player = context.getPlayer();
        return MathUtils.INT_POSITIVE.matcher(params).matches() && player.getInventory().getHeldItemSlot() == Integer.parseInt(params);
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
