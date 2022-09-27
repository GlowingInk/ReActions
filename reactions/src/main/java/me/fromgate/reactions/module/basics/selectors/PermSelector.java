package me.fromgate.reactions.module.basics.selectors;

import me.fromgate.reactions.selectors.Selector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PermSelector implements Selector {

    @Override
    public @NotNull String getName() {
        return "perm";
    }

    @Override
    public @NotNull Set<Player> getPlayers(String param) {
        if (param.isEmpty()) return Collections.emptySet();
        String[] perms = param.split(",\\s*");
        Set<Player> players = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers())
            for (String p : perms)
                if (player.hasPermission(p)) players.add(player);
        return players;
    }

}
