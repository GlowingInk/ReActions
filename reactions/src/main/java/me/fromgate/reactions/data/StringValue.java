package me.fromgate.reactions.data;

import me.fromgate.reactions.util.item.LegacyVirtualItem;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class StringValue implements DataValue {
    private String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public double asDouble() {
        return value.length();
    }

    @Override
    public boolean asBoolean() {
        return value.equalsIgnoreCase("true");
    }

    @Override
    public Location asLocation() {
        return LocationUtils.parseLocation(value, LocationUtils.ZERO_LOCATION);
    }

    @Override
    public ItemStack asItemStack() {
        return LegacyVirtualItem.fromString(value);
    }

    @Override
    public boolean set(String value) {
        this.value = value;
        return true;
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
        return false;
    }
}
