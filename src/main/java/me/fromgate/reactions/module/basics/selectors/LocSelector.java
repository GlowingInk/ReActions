package me.fromgate.reactions.module.basics.selectors;

import me.fromgate.reactions.selectors.Selector;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class LocSelector implements Selector {
    @Override
    public @NotNull String getName() {
        return "loc";
    }

    @Override
    public @NotNull Set<Player> getPlayers(String param) {
        Set<Player> players = new HashSet<>();
        if (param.isEmpty()) return players;
        Parameters params = Parameters.fromString(param, "loc");
        String locStr = params.getString("loc");
        if (locStr.isEmpty()) return players;
        Location loc = LocationUtils.parseLocation(locStr, null);
        if (loc == null) return players;
        loc.setX(loc.getBlockX() + 0.5);
        loc.setY(loc.getBlockY() + 0.5);
        loc.setZ(loc.getBlockZ() + 0.5);
        double radius = params.getDouble("radius", 1.0);
        radius *= radius;
        for (Player player : loc.getWorld().getPlayers())
            if (player.getLocation().distanceSquared(loc) <= radius) players.add(player);
        return players;
    }

}
