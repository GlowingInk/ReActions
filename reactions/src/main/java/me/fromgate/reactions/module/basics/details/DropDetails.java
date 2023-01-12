package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.data.DoubleValue;
import me.fromgate.reactions.data.ItemStackValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.activators.DropActivator;
import me.fromgate.reactions.util.collections.Maps;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-05-01.
 */
public class DropDetails extends Details {
    public static final String PICKUP_DELAY = "pickupdelay";
    public static final String ITEM = "item";

    private final ItemStack item;
    private final int pickupDelay;

    public DropDetails(Player p, Item item, int pickupDelay) {
        super(p);
        this.item = item.getItemStack();
        this.pickupDelay = pickupDelay;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return DropActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("droplocation", LocationUtils.locationToString(player.getLocation()));
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return new Maps.Builder<String, DataValue>()
                .put(CANCEL_EVENT, new BooleanValue(false))
                .put(PICKUP_DELAY, new DoubleValue(pickupDelay))
                .put(ITEM, new ItemStackValue(item))
                .build();
    }

    public ItemStack getItem() {return this.item;}

    public int getPickupDelay() {return this.pickupDelay;}
}
