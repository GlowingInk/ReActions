package fun.reactions.module.basic.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("BLOCK_CHECK")
public class BlockFlag implements Flag {
    @Override
    public @NotNull String getName() {
        return "BLOCK";
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Location loc = params.getOr("loc", LocationUtils::parseLocation, () -> env.hasPlayer() ? env.getPlayer().getLocation() : null);
        if (loc == null) return false;
        String istr = params.getString("block", "");
        if (istr.isEmpty()) return loc.getBlock().getType() != Material.AIR;
        Parameters block = Parameters.fromString(istr);
        String type = block.getString("type", block.getString(Parameters.ORIGIN_KEY, "AIR"));
        return loc.getBlock().getType().name().equalsIgnoreCase(type);
    }
}
