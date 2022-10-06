package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.ItemHeldActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-11-11.
 */
public class ItemHeldStorage extends Storage {

    private final int newSlot;
    private final int previousSlot;
    private final ItemStack newItem;
    private final ItemStack previousItem;

    public ItemHeldStorage(Player player, int newSlot, int previousSlot) {
        super(player);
        this.newSlot = newSlot;
        this.previousSlot = previousSlot;
        this.newItem = this.player.getInventory().getItem(newSlot);
        this.previousItem = this.player.getInventory().getItem(previousSlot);
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return ItemHeldActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        if (newItem != null) {
            VirtualItem vItem = VirtualItem.fromItem(newItem);
            tempVars.put("itemnew", vItem.toString());
            tempVars.put("itemnew-str", ItemUtils.toDisplayString(vItem.asParameters()));
        }
        if (previousItem != null) {
            VirtualItem vItem = VirtualItem.fromItem(previousItem);
            tempVars.put("itemprev", vItem.toString());
            tempVars.put("itemprev-str", ItemUtils.toDisplayString(vItem.asParameters()));
        }
        tempVars.put("slotNew", Integer.toString(newSlot + 1));
        tempVars.put("slotPrev", Integer.toString(previousSlot + 1));
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return MapBuilder.single(CANCEL_EVENT, new BooleanValue(false));
    }

    public int getNewSlot() {return this.newSlot;}

    public int getPreviousSlot() {return this.previousSlot;}

    public ItemStack getNewItem() {return this.newItem;}

    public ItemStack getPreviousItem() {return this.previousItem;}
}
