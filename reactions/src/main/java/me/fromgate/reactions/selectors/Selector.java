package me.fromgate.reactions.selectors;

import me.fromgate.reactions.util.naming.Named;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Selector extends Named {
    @NotNull Collection<Player> getPlayers(@NotNull String param);
}
