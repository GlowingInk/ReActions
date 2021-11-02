package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliases("BLOCK")
public class FlagBlock implements Flag {

    @Override
    public @NotNull String getName() {
        return "BLOCK_CHECK";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    public boolean check(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = context.getPlayer();
        Location loc = LocationUtils.parseLocation(params.getString("loc", ""), player.getLocation());
        if (loc == null) return false;
        String istr = params.getString("block", "");
        if (istr.isEmpty()) return loc.getBlock().getType() != Material.AIR;
        Parameters block = Parameters.fromString(istr);
        String type = block.getString("type", block.getString(Parameters.ORIGIN_KEY, "AIR"));
        return loc.getBlock().getType().name().equalsIgnoreCase(type);
    }
}
