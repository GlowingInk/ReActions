package fun.reactions.module.vault.selectors;

import fun.reactions.module.vault.external.RaVault;
import fun.reactions.selectors.Selector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class GroupSelector implements Selector {
    @Override
    public @NotNull String getName() {
        return "group";
    }

    @Override
    public void iteratePlayers(@NotNull String param, @NotNull Consumer<@Nullable Player> run) {
        if (!RaVault.isPermissionConnected()) return;
        if (param.isEmpty()) return;
        String[] group = param.split(",");
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String g : group) {
                if (RaVault.playerInGroup(player, g.trim())) {
                    run.accept(player);
                    break;
                }
            }
        }
    }
}
