package me.fromgate.reactions.module.basics.selectors;

import me.fromgate.reactions.externals.RaVault;
import me.fromgate.reactions.selectors.Selector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GroupSelector implements Selector {

    @Override
    public @NotNull String getName() {
        return "group";
    }

    @Override
    public @NotNull Set<Player> getPlayers(String param) {
        Set<Player> players = new HashSet<>();
        if (!RaVault.isPermissionConnected()) return players;
        if (param.isEmpty()) return players;
        String[] group = param.split(",\\s*");
        for (Player player : Bukkit.getOnlinePlayers())
            for (String g : group)
                if (RaVault.playerInGroup(player, g)) players.add(player);
        return players;
    }
}
