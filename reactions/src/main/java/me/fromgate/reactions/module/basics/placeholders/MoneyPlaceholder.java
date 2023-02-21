package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.externals.RaVault;
import me.fromgate.reactions.logic.environment.Environment;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names("balance")
public class MoneyPlaceholder implements Placeholder.Keyed {

    @Override
    public @Nullable String processPlaceholder(@NotNull Environment env, @NotNull String key, @NotNull String param) {
        if (env.getPlayer() == null) return "0";
        if (param.isEmpty()) {
            return Double.toString(RaVault.getBalance(env.getPlayer()));
        } else {
            return Double.toString(RaVault.getBalance(env.getPlayer(), param));
        }
    }

    @Override
    public @NotNull String getName() {
        return "money";
    }
}
