package me.fromgate.reactions.selectors;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Selector {
    @NotNull String getName();

    @NotNull Collection<Player> getPlayers(@NotNull String param);
}
