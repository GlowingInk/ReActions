package fun.reactions.module.basic.selectors;

import fun.reactions.selectors.Selector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class PlayerSelector implements Selector {
    @Override
    public @NotNull String getName() {
        return "player";
    }

    @Override
    public void iteratePlayers(@NotNull String param, @NotNull Consumer<@Nullable Player> run) {
        if (param.isEmpty()) return;
        if (param.equalsIgnoreCase("~null")) {
            run.accept(null);
        } else if (param.equalsIgnoreCase("~all")) {
            Bukkit.getOnlinePlayers().forEach(run);
        } else {
            for (String playerName : param.split(",")) {
                Player targetPlayer = Bukkit.getPlayerExact(playerName.trim());
                if ((targetPlayer != null) && (targetPlayer.isOnline())) run.accept(targetPlayer);
            }
        }
    }
}
