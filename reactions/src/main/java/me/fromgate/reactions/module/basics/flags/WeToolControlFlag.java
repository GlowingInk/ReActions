package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.externals.worldedit.RaWorldEdit;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.logic.environment.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// TODO: WorldEdit module
/**
 * Created by MaxDikiy on 11/10/2017.
 */
public class WeToolControlFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String params) {
        Player player = env.getPlayer();
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
