package me.fromgate.reactions.data;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface DataValue { // TODO: Should probably be refactored.. somehow
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
