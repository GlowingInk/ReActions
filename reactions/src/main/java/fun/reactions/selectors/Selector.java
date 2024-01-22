package fun.reactions.selectors;

import fun.reactions.util.naming.Named;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public interface Selector extends Named {
    default @NotNull Set<@Nullable Player> getPlayers(@NotNull String param) {
        Set<Player> players = new HashSet<>();
        iteratePlayers(param, players::add);
        return players;
    }

    void iteratePlayers(@NotNull String param, @NotNull Consumer<@Nullable Player> run);
}
