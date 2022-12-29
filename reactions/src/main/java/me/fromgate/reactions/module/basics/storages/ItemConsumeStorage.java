package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.ItemConsumeActivator;
import me.fromgate.reactions.util.collections.Maps;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ItemConsumeStorage extends Storage {

    private final ItemStack item;
    private final boolean mainHand;

    public ItemConsumeStorage(Player p, ItemStack item, boolean mainHand) {
        super(p);
        this.item = item;
        this.mainHand = mainHand;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return ItemConsumeActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        if (item != null) {
            VirtualItem vItem = VirtualItem.fromItem(item);
            tempVars.put("item", vItem.toString());
            tempVars.put("item-str", ItemUtils.toDisplayString(vItem.asParameters()));
        }
        tempVars.put("hand", mainHand ? "MAIN" : "OFF");
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return Maps.Builder.single(CANCEL_EVENT, new BooleanValue(false));
    }

    public ItemStack getItem() {return this.item;}

    public boolean isMainHand() {return this.mainHand;}
}
