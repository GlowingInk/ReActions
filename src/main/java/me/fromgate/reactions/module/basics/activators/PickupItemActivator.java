package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.PickupItemStorage;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Created by MaxDikiy on 2017-09-04.
 */
public class PickupItemActivator extends Activator {
    // TODO: Store VirtualItem
    private final String itemStr;

    private PickupItemActivator(ActivatorLogic base, String item) {
        super(base);
        this.itemStr = item;
    }

    public static PickupItemActivator create(ActivatorLogic base, Parameters param) {
        String item = param.getString("item", param.toString());
        return new PickupItemActivator(base, item);
    }

    public static PickupItemActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String item = cfg.getString("item", "");
        return new PickupItemActivator(base, item);
    }

    @Override
    public boolean checkStorage(Storage event) {
        PickupItemStorage pie = (PickupItemStorage) event;
        return checkItem(pie.getItem());
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("item", itemStr);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    private boolean checkItem(ItemStack item) {
        if (this.itemStr.isEmpty()) return true;
        return ItemUtils.compareItemStr(item, this.itemStr, true);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "item:" + this.itemStr +
                ")";
        return sb;
    }
}
