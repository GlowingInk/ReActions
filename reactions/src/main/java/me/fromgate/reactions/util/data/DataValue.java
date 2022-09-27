package me.fromgate.reactions.util.data;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface DataValue {
    String asString();

    double asDouble();

    boolean asBoolean();

    Location asLocation();

    ItemStack asItemStack();

    boolean set(String value);

    boolean set(double value);

    boolean set(boolean value);

    boolean set(Location value);

    boolean set(ItemStack value);
}
