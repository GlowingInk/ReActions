package fun.reactions.module.basics.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

/**
 * @author MaxDikiy
 * @since 10/01/2017
 */
public class NumCompareFlags implements Flag {
    private final boolean greater;

    public NumCompareFlags(boolean greater) {
        this.greater = greater;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        double paramValue = params.getDouble("param");
        double value = params.getDouble("value");
        if (greater) {
            env.getVariables().set("gparam", Double.toString(paramValue));
            return paramValue > value;
        } else {
            env.getVariables().set("lparam", Double.toString(paramValue));
            return paramValue < value;
        }
    }

    @Override
    public @NotNull String getName() {
        return greater ? "GREATER" : "LOWER";
    }

}
