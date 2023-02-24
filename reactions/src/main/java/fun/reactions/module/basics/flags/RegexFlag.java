package fun.reactions.module.basics.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public class RegexFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        Parameters params = Parameters.fromString(content);
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
