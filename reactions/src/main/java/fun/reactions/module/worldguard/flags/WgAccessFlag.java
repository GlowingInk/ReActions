package fun.reactions.module.worldguard.flags;

import com.sk89q.worldguard.protection.flags.Flags;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.worldguard.external.WGBridge;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WgAccessFlag implements Flag, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Location loc = params.get("loc", LocationUtils::parseLocation);
        if (loc == null) return true;
        // TODO Check flags?
        return WGBridge.isAccessible(player, loc, Flags.BUILD);
    }

    @Override
    public @NotNull String getName() {
        return "WG_ACCESS";
    }
}
