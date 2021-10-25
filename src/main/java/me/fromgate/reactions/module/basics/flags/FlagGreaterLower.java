package me.fromgate.reactions.module.basics.flags;

import lombok.AllArgsConstructor;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 10/1/2017.
 */
@AllArgsConstructor
public class FlagGreaterLower extends Flag {
    private final boolean greater;

    @Override
    public boolean check(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        double paramValue = params.getDouble("param", 0d);
        double value = params.getDouble("value", 0d);
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
