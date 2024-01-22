package fun.reactions.module.worldguard.selectors;

import fun.reactions.module.worldguard.external.RaWorldGuard;
import fun.reactions.selectors.Selector;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RegionSelector implements Selector {
    @Override
    public @NotNull String getName() {
        return "region";
    }

    @Override
    public void iteratePlayers(@NotNull String param, @NotNull Consumer<@Nullable Player> run) {
        if (param.isEmpty()) return;
        for (String regionName : param.split(",")) {
            RaWorldGuard.playersInRegion(regionName.trim()).forEach(run);
        }
    }
}
