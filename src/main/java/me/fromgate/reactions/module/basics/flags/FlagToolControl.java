package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.externals.worldedit.RaWorldEdit;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// TODO: WorldEdit module
/**
 * Created by MaxDikiy on 11/10/2017.
 */
public class FlagToolControl implements Flag {
    @Override
    public boolean check(@NotNull RaContext context, @NotNull String params) {
        Player player = context.getPlayer();
        return Boolean.parseBoolean(params) == RaWorldEdit.isToolControl(player);
    }

    @Override
    public @NotNull String getName() {
        return "WE_TOOLCONTROL";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
