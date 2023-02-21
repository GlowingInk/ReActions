package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.logic.environment.Environment;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public class RegexFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
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
