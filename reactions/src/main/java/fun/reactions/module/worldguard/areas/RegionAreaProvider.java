package fun.reactions.module.worldguard.areas;

import fun.reactions.areas.AreaBounds;
import fun.reactions.areas.AreaProvider;
import fun.reactions.module.worldguard.external.RaWorldGuard;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RegionAreaProvider implements AreaProvider {
    @Override
    public @Nullable AreaBounds getBounds(@NotNull String id) {
        List<Location> minMax = RaWorldGuard.getRegionMinMaxLocations(id);
        return minMax.isEmpty() ? null : new AreaBounds(minMax.get(0), minMax.get(1));
    }

    @Override
    public boolean contains(@NotNull String id, @NotNull Location location) {
        return RaWorldGuard.isLocationInRegion(location, id);
    }

    @Override
    public @NotNull String getName() {
        return "region";
    }
}
