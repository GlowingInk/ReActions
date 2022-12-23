package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 10/1/2017.
 */
public class FlagGreaterLower implements Flag {
    private final boolean greater;

    public FlagGreaterLower(boolean greater) {
        this.greater = greater;
    }

    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        double paramValue = params.getDouble("param");
        double value = params.getDouble("value");
        if (greater) {
            context.setVariable("gparam", Double.toString(paramValue));
            return paramValue > value;
        } else {
            context.setVariable("lparam", Double.toString(paramValue));
            return paramValue < value;
        }
    }

    @Override
    public @NotNull String getName() {
        return greater ? "GREATER" : "LOWER";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
