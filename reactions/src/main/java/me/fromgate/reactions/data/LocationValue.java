package me.fromgate.reactions.data;

import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LocationValue implements DataValue {
    private Location value;

    public LocationValue(Location value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return LocationUtils.locationToString(value);
    }

    @Override
    public double asDouble() {
        return value.getX() + value.getY() + value.getZ();
    }

    @Override
    public boolean asBoolean() {
        return value.isWorldLoaded();
    }

    @Override
    public Location asLocation() {
        return value;
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(Material.STONE);
    }

    @Override
    public boolean set(String value) {
        Location loc = LocationUtils.parseLocation(value, null);
        if (loc != null) {
            this.value = loc;
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
        this.value = value;
        return true;
    }

    @Override
    public boolean set(ItemStack value) {
        return false;
    }
}
