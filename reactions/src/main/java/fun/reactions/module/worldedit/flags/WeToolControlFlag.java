package fun.reactions.module.worldedit.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.worldedit.external.RaWorldEdit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// TODO: WorldEdit module
/**
 * @author MaxDikiy
 * @since 11/10/2017
 */
public class WeToolControlFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Player player = env.getPlayer();
        return Boolean.parseBoolean(paramsStr) == RaWorldEdit.isToolControl(player);
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
