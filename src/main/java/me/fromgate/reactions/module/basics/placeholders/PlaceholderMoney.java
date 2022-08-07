package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.externals.RaEconomics;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.alias.Aliases;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Aliases("balance")
public class PlaceholderMoney implements Placeholder.Equal {

    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        Map<String, String> params = RaEconomics.getBalances(context.getPlayer());
        return params.getOrDefault(key, null);
    }

    @Override
    public @NotNull String getId() {
        return "money";
    }
}
