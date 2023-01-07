package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.externals.RaVault;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names("balance")
public class PlaceholderMoney implements Placeholder.Keyed {

    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        if (context.getPlayer() == null) return "0";
        if (param.isEmpty()) {
            return Double.toString(RaVault.getBalance(context.getPlayer()));
        } else {
            return Double.toString(RaVault.getBalance(context.getPlayer(), param));
        }
    }

    @Override
    public @NotNull String getName() {
        return "money";
    }
}
