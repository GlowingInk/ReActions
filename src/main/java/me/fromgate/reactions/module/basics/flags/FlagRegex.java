package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public class FlagRegex extends Flag {
    @Override
    public boolean check(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        String regex = params.getString("regex", "");
        if (regex.isEmpty()) return false;
        String value = params.getString("value", "");
        if (value.isEmpty()) return false;
        return value.matches(regex);
    }

    @Override
    public @NotNull String getName() {
        return "REGEX";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
