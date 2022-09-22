package me.fromgate.reactions.logic.activators;

import org.bukkit.World;

public interface Locatable {
    boolean isLocatedAt(World world, int x, int y, int z);
}
