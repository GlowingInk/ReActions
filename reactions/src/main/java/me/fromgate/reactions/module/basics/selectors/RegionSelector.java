package me.fromgate.reactions.module.basics.selectors;

import me.fromgate.reactions.externals.worldguard.RaWorldGuard;
import me.fromgate.reactions.selectors.Selector;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class RegionSelector implements Selector {

    @Override
    public @NotNull String getName() {
        return "region";
    }

    @Override
    public @NotNull Set<Player> getPlayers(@NotNull String regionStr) {
        if (!RaWorldGuard.isConnected()) return Set.of();
        if (regionStr.isEmpty()) return Set.of();
        String[] arrRegion = regionStr.split(",");
        Set<Player> players = new HashSet<>();
        for (String regionName : arrRegion) {
            players.addAll(RaWorldGuard.playersInRegion(regionName.trim()));
        }
        return players;
    }

}
