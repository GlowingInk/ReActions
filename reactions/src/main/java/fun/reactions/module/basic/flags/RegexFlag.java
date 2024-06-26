package fun.reactions.module.basic.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
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

}
