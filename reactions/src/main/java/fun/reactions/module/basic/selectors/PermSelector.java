package fun.reactions.module.basic.selectors;

import fun.reactions.selectors.Selector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PermSelector implements Selector {

    @Override
    public @NotNull String getName() {
        return "perm";
    }

    @Override
    public @NotNull Set<Player> getPlayers(@NotNull String param) {
        if (param.isEmpty()) return Set.of();
        String[] perms = param.split(",");
        Set<Player> players = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String p : perms) {
                if (player.hasPermission(p.trim())) {
                    players.add(player);
                    break;
                }
            }
        }
        return players;
    }

}
