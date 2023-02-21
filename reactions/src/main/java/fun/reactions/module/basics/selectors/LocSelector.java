package fun.reactions.module.basics.selectors;

import fun.reactions.selectors.Selector;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
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
    public @NotNull Set<Player> getPlayers(@NotNull String param) {
        if (param.isEmpty()) return Set.of();
        Parameters params = Parameters.fromString(param, "loc");
        String locStr = params.getString("loc");
        if (locStr.isEmpty()) return Set.of();
        Location loc = LocationUtils.parseLocation(locStr, null);
        if (loc == null) return Set.of();
        loc.setX(loc.getBlockX() + 0.5);
        loc.setY(loc.getBlockY() + 0.5);
        loc.setZ(loc.getBlockZ() + 0.5);
        double radius = params.getDouble("radius", 1.0);
        radius *= radius;
        Set<Player> players = new HashSet<>();
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) <= radius) {
                players.add(player);
            }
        }
        return players;
    }

}
