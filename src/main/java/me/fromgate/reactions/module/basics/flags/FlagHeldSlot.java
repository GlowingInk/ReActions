package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.NumberUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Alias("SLOT")
public class FlagHeldSlot extends Flag {
    @Override
    protected boolean check(@NotNull RaContext context, @NotNull Parameters params) {
        Player player = context.getPlayer();
        return NumberUtils.INT_POSITIVE.matcher(params.toString()).matches() && player.getInventory().getHeldItemSlot() == Integer.parseInt(params.toString());
    }

    @Override
    public @NotNull String getName() {
        return "HELP_SLOT";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    protected boolean isParameterized() {
        return false;
    }
}
