package fun.reactions.module.basics.placeholders;

import fun.reactions.externals.RaVault;
import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names("balance")
public class MoneyPlaceholder implements Placeholder.Keyed {

    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String param) {
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
