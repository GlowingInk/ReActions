
package me.fromgate.reactions.module.basics.storages;

import lombok.Getter;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.WEChangeActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.BooleanValue;
import me.fromgate.reactions.util.data.DataValue;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;
/**
 * Created by MaxDikiy on 17/10/2017.
 */
@Getter
public class WeChangeStorage extends Storage {

    private final Location location;
    private final Material blockType;

    public WeChangeStorage(Player player, Location location, Material blockType) {
        super(player);
        this.location = location;
        this.blockType = blockType;
    }

    @Override
    public Class<? extends Activator> getType() {
        return WEChangeActivator.class;
    }

    @Override
    protected Map<String, String> prepareVariables() {
        return new MapBuilder<String, String>()
                .put("blocktype", blockType.name())
                .put("blocklocation", LocationUtils.locationToString(location))
                .build();
    }

    @Override
    protected Map<String, DataValue> prepareChangeables() {
        return MapBuilder.single(CANCEL_EVENT, new BooleanValue(false));
    }
}
