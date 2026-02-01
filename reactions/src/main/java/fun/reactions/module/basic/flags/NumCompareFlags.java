package fun.reactions.module.basic.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

/**
 * @author MaxDikiy
 * @since 10/01/2017
 */
public record NumCompareFlags(@NotNull Type flagType) implements Flag { // TODO Very unintuitive
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        double left = params.getDouble("param");
        double right = params.getDouble("value");
        boolean equal = params.getBoolean("or-equal");
        if (flagType == Type.GREATER) {
            env.getVariables().set("gparam", Double.toString(left));
            return equal
                    ? left >= right
                    : left > right;
        } else {
            env.getVariables().set("lparam", Double.toString(left));
            return equal
                    ? left <= right
                    : left < right;
        }
    }

    @Override
    public @NotNull String getName() {
        return flagType.name();
    }

    public enum Type {
        GREATER, LOWER
    }
}
