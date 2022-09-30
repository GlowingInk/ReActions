package me.fromgate.reactions.data;

import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BooleanValue implements DataValue {
    private boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return Boolean.toString(value);
    }

    @Override
    public double asDouble() {
        return value ? 1 : 0;
    }

    @Override
    public boolean asBoolean() {
        return value;
    }

    @Override
    public Location asLocation() {
        return LocationUtils.ZERO_LOCATION;
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(Material.STONE);
    }

    @Override
    public boolean set(String value) {
        if (value.equalsIgnoreCase("true")) {
            this.value = true;
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            this.value = false;
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
        this.value = value;
        return true;
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
