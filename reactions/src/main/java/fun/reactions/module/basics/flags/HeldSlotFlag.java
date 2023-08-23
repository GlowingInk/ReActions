package fun.reactions.module.basics.flags;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"SLOT", "HELP_SLOT"}) // My legacy typo...
public class HeldSlotFlag implements Flag, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        return NumberUtils.asInteger(paramsStr, -1) == player.getInventory().getHeldItemSlot();
    }

    @Override
    public @NotNull String getName() {
        return "HELD_SLOT";
    }
}
