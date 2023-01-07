package me.fromgate.reactions.logic.activators;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface Locatable {
    boolean isLocatedAt(@NotNull World world, int x, int y, int z);
}
