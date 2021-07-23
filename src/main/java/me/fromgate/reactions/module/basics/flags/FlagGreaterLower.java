package me.fromgate.reactions.module.basics.flags;

import lombok.AllArgsConstructor;
import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;

/**
 * Created by MaxDikiy on 10/1/2017.
 */
@AllArgsConstructor
public class FlagGreaterLower implements OldFlag {
    private final boolean greater;

    @Override
    public boolean checkFlag(RaContext context, String param) {
        Parameters params = Parameters.fromString(param, "unknown");
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
}
