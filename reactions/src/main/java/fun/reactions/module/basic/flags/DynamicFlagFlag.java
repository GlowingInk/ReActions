package fun.reactions.module.basic.flags;

import fun.reactions.ReActions;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"DYNFLAG", "DYN_FLAG"})
public class DynamicFlagFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        ReActions.Platform platform = env.getPlatform();
        Parameters params = Parameters.fromString(paramsStr);
        var flag = params.getOptional("type", platform.getActivities()::getFlag);
        if (flag.isEmpty()) {
            env.warn("Failed to prepare dynamic flag: flag type '" + flag.key() + "' doesn't exist");
            return false;
        }
        String content = params.getString("content");
        boolean invert = params.getBoolean("inverted");

        return flag.get().proceed(env, content) != invert;
    }

    @Override
    public @NotNull String getName() {
        return "DYNAMIC_FLAG";
    }
}
