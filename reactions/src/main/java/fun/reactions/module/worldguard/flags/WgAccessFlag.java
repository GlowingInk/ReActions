package fun.reactions.module.worldguard.flags;

import com.sk89q.worldguard.protection.flags.Flags;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.worldguard.external.WGBridge;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class WgAccessFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        Parameters params = Parameters.fromString(content);
        Location loc = params.get("loc", LocationUtils::parseLocation);
        if (loc == null) return true;
        // TODO params.get("flag"...)
        return WGBridge.isAccessible(env.getPlayer(), loc, Flags.BUILD);
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "WG_ACCESS";
    }
}
