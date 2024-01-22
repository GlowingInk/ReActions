package fun.reactions.module.basic.selectors;

import fun.reactions.selectors.Selector;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class WorldSelector implements Selector {
    @Override
    public @NotNull String getName() {
        return "world";
    }

    @Override
    public void iteratePlayers(@NotNull String param, @NotNull Consumer<@Nullable Player> run) {
        if (!param.isEmpty()) {
            for (String worldName : param.split(",")) {
                World world = Bukkit.getWorld(worldName.trim());
                if (world == null) continue;
                world.getPlayers().forEach(run);
            }
        }
    }
}
