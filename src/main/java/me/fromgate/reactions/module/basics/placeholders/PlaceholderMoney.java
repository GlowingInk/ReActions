package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.externals.RaEconomics;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Alias("balance")
public class PlaceholderMoney implements Placeholder.Equal {

    @Override
    public @NotNull String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        Map<String, String> params = RaEconomics.getBalances(context.getPlayer());
        return params.getOrDefault(key, null);
    }

    @Override
    public @NotNull String getId() {
        return "money";
    }
}
