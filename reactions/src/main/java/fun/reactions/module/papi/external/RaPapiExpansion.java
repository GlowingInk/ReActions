package fun.reactions.module.papi.external;

import fun.reactions.PersistentVariablesManager;
import fun.reactions.ReActions;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class RaPapiExpansion extends PlaceholderExpansion {
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "reactions";
    }

    @Override
    public @NotNull String getAuthor() {
        return "fromgate";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.0.3";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String param) {
        PersistentVariablesManager variables = ReActions.getVariables();

        if (StringUtil.startsWithIgnoreCase(param, "varp:")) {
            return player == null || player.getName() == null
                    ? null
                    : variables.getVariable(player.getName(), param.substring(5));
        } else if (StringUtil.startsWithIgnoreCase(param, "var:")) {
            String[] split = param.substring(4).split("\\.", 2);
            return split.length > 1
                    ? variables.getVariable(split[0], split[1])
                    : variables.getVariable(null, split[0]);
        }

        return null;
    }
}
