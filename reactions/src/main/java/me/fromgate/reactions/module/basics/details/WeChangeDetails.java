
package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.activators.WEChangeActivator;
import me.fromgate.reactions.util.collections.Maps;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by MaxDikiy on 17/10/2017.
 */
public class WeChangeDetails extends Details {

    private final Location location;
    private final Material blockType;

    public WeChangeDetails(Player player, Location location, Material blockType) {
        super(player);
        this.location = location;
        this.blockType = blockType;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return WEChangeActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        return new Maps.Builder<String, String>()
                .put("blocktype", blockType.name())
                .put("blocklocation", LocationUtils.locationToString(location))
                .build();
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return Maps.Builder.single(CANCEL_EVENT, new BooleanValue(false));
    }

    public Location getLocation() {return this.location;}

    public Material getBlockType() {return this.blockType;}
}
