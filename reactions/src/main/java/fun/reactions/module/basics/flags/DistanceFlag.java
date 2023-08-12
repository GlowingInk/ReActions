package fun.reactions.module.basics.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"NEARBY", "NEAR_TO", "IS_NEAR", "NEAR"})
public class DistanceFlag implements Flag {
    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Location loc = params.get("loc", LocationUtils::parseLocation);
        if (loc == null) {
            if (env.hasPlayer()) {
                loc = env.getPlayer().getLocation();
            } else {
                return false;
            }
        }
        Location center = params.get("target", LocationUtils::parseLocation);
        if (center == null) return false;
        double distance = params.getDouble("blocks");
        return distance <= 0
                ? LocationUtils.equalsPositionally(center, loc)
                : center.getWorld() == loc.getWorld() && center.distanceSquared(loc) <= distance*distance;
    }

    @Override
    public @NotNull String getName() {
        return "DISTANCE";
    }
}
