package fun.reactions.module.basic.selectors;

import fun.reactions.selectors.Selector;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class WorldSelector implements Selector {

    @Override
    public @NotNull String getName() {
        return "world";
    }

    @Override
    public @NotNull Set<Player> getPlayers(@NotNull String worldNames) {
        Set<Player> players = new HashSet<>();
        if (!worldNames.isEmpty()) {
            String[] arrWorlds = worldNames.split(",");
            for (String worldName : arrWorlds) {
                World world = Bukkit.getWorld(worldName.trim());
                if (world == null) continue;
                players.addAll(world.getPlayers());
            }
        }
        return players;
    }

}
