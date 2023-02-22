package fun.reactions.module.vault.selectors;

import fun.reactions.module.vault.external.RaVault;
import fun.reactions.selectors.Selector;
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
    public @NotNull Set<Player> getPlayers(@NotNull String param) {
        if (!RaVault.isPermissionConnected()) return Set.of();
        if (param.isEmpty()) return Set.of();
        Set<Player> players = new HashSet<>();
        String[] group = param.split(",");
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String g : group) {
                if (RaVault.playerInGroup(player, g.trim())) {
                    players.add(player);
                    break;
                }
            }
        }
        return players;
    }
}
