package fun.reactions.module.basic.selectors;

import fun.reactions.selectors.Selector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class PermSelector implements Selector {
    @Override
    public @NotNull String getName() {
        return "perm";
    }

    @Override
    public void iteratePlayers(@NotNull String param, @NotNull Consumer<@Nullable Player> run) {
        if (param.isEmpty()) return;
        String[] perms = param.split(",");
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String p : perms) {
                if (player.hasPermission(p.trim())) {
                    run.accept(player);
                    break;
                }
            }
        }
    }
}
