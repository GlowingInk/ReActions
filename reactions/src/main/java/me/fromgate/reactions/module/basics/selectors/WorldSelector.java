package me.fromgate.reactions.module.basics.selectors;

import me.fromgate.reactions.selectors.Selector;
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
    public @NotNull Set<Player> getPlayers(String worldNames) {
        Set<Player> players = new HashSet<>();
        if (!worldNames.isEmpty()) {
            String[] arrWorlds = worldNames.split(",\\s*");
            for (String worldName : arrWorlds) {
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                players.addAll(world.getPlayers());
            }
        }
        return players;
    }

}
