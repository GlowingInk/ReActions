package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FlagBlock implements OldFlag {

    @Override
    public boolean checkFlag(RaContext context, String param) {
        Player player = context.getPlayer();
        Parameters params = Parameters.fromString(param, "loc");
        Location loc = LocationUtils.parseLocation(params.getString("loc", ""), player.getLocation());
        if (loc == null) return false;
        String istr = params.getString("block", "");
        if (istr.isEmpty()) return loc.getBlock().getType() != Material.AIR;
        Parameters block = Parameters.fromString(istr);
        String type = block.getString("type", block.getString("param-line", "AIR"));
        return loc.getBlock().getType().name().equalsIgnoreCase(type);
    }

}
