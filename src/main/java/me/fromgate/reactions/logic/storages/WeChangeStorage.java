
package me.fromgate.reactions.logic.storages;

import lombok.Getter;
import me.fromgate.reactions.logic.triggers.ActivatorType;
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
        super(player, ActivatorType.WE_CHANGE);
        this.location = location;
        this.blockType = blockType;
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
