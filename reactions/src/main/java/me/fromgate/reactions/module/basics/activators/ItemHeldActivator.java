package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.ItemHeldStorage;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Created by MaxDikiy on 2017-11-11.
 */
public class ItemHeldActivator extends Activator {
    private final int previousSlot;
    private final int newSlot;
    // TODO: Store VirtualItem
    private final String itemNewStr;
    private final String itemPrevStr;

    private ItemHeldActivator(ActivatorLogic base, String itemPrevStr, String itemNewStr, int previousSlot, int newSlot) {
        super(base);
        this.itemNewStr = itemNewStr;
        this.itemPrevStr = itemPrevStr;
        this.previousSlot = previousSlot;
        this.newSlot = newSlot;
    }

    public static ItemHeldActivator create(ActivatorLogic base, Parameters param) {
        String itemNewStr = param.getString("itemnew", "");
        String itemPrevStr = param.getString("itemprev", "");
        int newSlot = param.getInteger("slotnew", 1);
        int previousSlot = param.getInteger("slotprev", 1);
        return new ItemHeldActivator(base, itemPrevStr, itemNewStr, --newSlot, --previousSlot);
    }

    public static ItemHeldActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String itemNewStr = cfg.getString("item-new");
        String itemPrevStr = cfg.getString("item-prev");
        int newSlot = cfg.getInt("slot-new", 1);
        int previousSlot = cfg.getInt("slot-prev", 1);
        return new ItemHeldActivator(base, itemPrevStr, itemNewStr, --newSlot, --previousSlot);
    }

    @Override
    public boolean checkStorage(Storage event) {
        ItemHeldStorage ihe = (ItemHeldStorage) event;
        ItemStack itemNew = ihe.getNewItem();
        ItemStack itemPrev = ihe.getPreviousItem();
        if (!this.itemNewStr.isEmpty() && (!ItemUtils.compareItemStr(itemNew, this.itemNewStr)))
            return false;
        if (!this.itemPrevStr.isEmpty() && (!ItemUtils.compareItemStr(itemPrev, this.itemPrevStr)))
            return false;
        if (newSlot > -1 && newSlot != ihe.getNewSlot()) return false;
        return previousSlot <= -1 || previousSlot == ihe.getPreviousSlot();
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("item-new", itemNewStr);
        cfg.set("item-prev", itemPrevStr);
        cfg.set("slot-new", newSlot + 1);
        cfg.set("slot-prev", previousSlot + 1);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "itemnew:" + (itemNewStr.isEmpty() ? "-" : itemNewStr) +
                " itemprev:" + (itemPrevStr.isEmpty() ? "-" : itemPrevStr) +
                " slotnew:" + (newSlot + 1) +
                " slotprev:" + (previousSlot + 1) +
                ")";
        return sb;
    }
}
