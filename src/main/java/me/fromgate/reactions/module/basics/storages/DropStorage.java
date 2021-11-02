package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.*;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.BooleanValue;
import me.fromgate.reactions.util.data.DataValue;
import me.fromgate.reactions.util.data.DoubleValue;
import me.fromgate.reactions.util.data.ItemStackValue;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-05-01.
 */
public class DropStorage extends Storage {
    public static final String PICKUP_DELAY = "pickupdelay";
    public static final String ITEM = "item";

    private final ItemStack item;
    private final int pickupDelay;

    public DropStorage(Player p, Item item, int pickupDelay) {
        super(p);
        this.item = item.getItemStack();
        this.pickupDelay = pickupDelay;
    }

    @Override
    public Class<? extends Activator> getType() {
        return DropActivator.class;
    }

    @Override
    protected Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("droplocation", LocationUtils.locationToString(player.getLocation()));
        return tempVars;
    }

    @Override
    protected Map<String, DataValue> prepareChangeables() {
        return new MapBuilder<String, DataValue>()
                .put(CANCEL_EVENT, new BooleanValue(false))
                .put(PICKUP_DELAY, new DoubleValue(pickupDelay))
                .put(ITEM, new ItemStackValue(item))
                .build();
    }

    public ItemStack getItem() {return this.item;}

    public int getPickupDelay() {return this.pickupDelay;}
}
