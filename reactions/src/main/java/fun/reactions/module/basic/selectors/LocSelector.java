package fun.reactions.module.basic.selectors;

import fun.reactions.selectors.Selector;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class LocSelector implements Selector {
    @Override
    public @NotNull String getName() {
        return "loc";
    }

    @Override
    public void iteratePlayers(@NotNull String param, @NotNull Consumer<@Nullable Player> run) {
        if (param.isEmpty()) return;
        Parameters params = Parameters.fromString(param, "loc");
        String locStr = params.getString("loc");
        if (locStr.isEmpty()) return;
        Location loc = LocationUtils.parseLocation(locStr, null);
        if (loc == null) return;
        loc.setX(loc.getBlockX() + 0.5);
        loc.setY(loc.getBlockY() + 0.5);
        loc.setZ(loc.getBlockZ() + 0.5);
        double radius = params.getDouble("radius", 1.0);
        radius *= radius;
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) <= radius) {
                run.accept(player);
            }
        }
    }
}
