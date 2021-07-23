package me.fromgate.reactions.module.basics.storages;

import lombok.Getter;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.DamageByBlockActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.BooleanValue;
import me.fromgate.reactions.util.data.DataValue;
import me.fromgate.reactions.util.data.DoubleValue;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-07-23.
 */
@Getter
public class DamageByBlockStorage extends Storage {

    private final Block blockDamager;
    private final DamageCause cause;
    private final double damage;


    public DamageByBlockStorage(Player player, Block blockDamager, double damage, DamageCause cause) {
        super(player);
        this.blockDamager = blockDamager;
        this.damage = damage;
        this.cause = cause;
    }

    @Override
    public Class<? extends Activator> getType() {
        return DamageByBlockActivator.class;
    }

    @Override
    protected Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("blocklocation", LocationUtils.locationToString(blockDamager.getLocation()));
        tempVars.put("blocktype", blockDamager.getType().name());
        tempVars.put("block", ItemUtils.itemFromBlock(blockDamager).toString());
        tempVars.put("cause", cause.name());
        return tempVars;
    }

    @Override
    protected Map<String, DataValue> prepareChangeables() {
        return new MapBuilder<String, DataValue>()
                .put(CANCEL_EVENT, new BooleanValue(false))
                .put(DamageStorage.DAMAGE, new DoubleValue(damage))
                .build();
    }
}
