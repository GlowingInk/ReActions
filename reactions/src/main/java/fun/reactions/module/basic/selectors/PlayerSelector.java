package fun.reactions.module.basic.selectors;

import fun.reactions.selectors.Selector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PlayerSelector implements Selector {

    @Override
    public @NotNull String getName() {
        return "player";
    }

    @Override
    public @NotNull Set<Player> getPlayers(@NotNull String param) {
        Set<Player> players = new HashSet<>();
        if (param.isEmpty()) return players;
        if (param.equalsIgnoreCase("~null")) {
            players.add(null);
        } else if (param.equalsIgnoreCase("~all")) {
            players.addAll(Bukkit.getOnlinePlayers());
        } else {
            String[] arrPlayers = param.split(",");
            for (String playerName : arrPlayers) {
                Player targetPlayer = Bukkit.getPlayerExact(playerName.trim());
                if ((targetPlayer != null) && (targetPlayer.isOnline())) players.add(targetPlayer);
            }
        }
        return players;
    }
}
