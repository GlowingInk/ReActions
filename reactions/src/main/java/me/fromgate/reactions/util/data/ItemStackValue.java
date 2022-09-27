package me.fromgate.reactions.util.data;

import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class ItemStackValue implements DataValue {
    private ItemStack value;

    public ItemStackValue(ItemStack value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return ItemUtils.itemToString(value);
    }

    @Override
    public double asDouble() {
        return value.getAmount();
    }

    @Override
    public boolean asBoolean() {
        return value.hasItemMeta();
    }

    @Override
    public Location asLocation() {
        return LocationUtils.ZERO_LOCATION;
    }

    @Override
    public ItemStack asItemStack() {
        return value;
    }

    @Override
    public boolean set(String value) {
        ItemStack item = VirtualItem.fromString(value);
        if (item != null) {
            this.value = item;
            return true;
        }
        return false;
    }

    @Override
    public boolean set(double value) {
        return false;
    }

    @Override
    public boolean set(boolean value) {
        return false;
    }

    @Override
    public boolean set(Location value) {
        return false;
    }

    @Override
    public boolean set(ItemStack value) {
        this.value = value;
        return true;
    }
}
