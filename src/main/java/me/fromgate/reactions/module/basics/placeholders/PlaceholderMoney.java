package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.externals.RaEconomics;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;

import java.util.Map;

@Alias("balance")
public class PlaceholderMoney implements Placeholder.Equal {

    @Override
    public String processPlaceholder(RaContext context, String key, String param) {
        Map<String, String> params = RaEconomics.getBalances(context.getPlayer());
        return params.getOrDefault(key, null);
    }

    @Override
    public String getId() {
        return "money";
    }
}
