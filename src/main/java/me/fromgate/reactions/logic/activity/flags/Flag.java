package me.fromgate.reactions.logic.activity.flags;

import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public abstract class Flag {
    public boolean check(@NotNull RaContext context, @NotNull String paramsStr) {
        return check(
                context,
                isParameterized() ?
                    Parameters.fromString(paramsStr) :
                    Parameters.noParse(paramsStr)
        );
    }

    protected abstract boolean check(@NotNull RaContext context, @NotNull Parameters params);

    @NotNull
    public abstract String getName();

    public abstract boolean requiresPlayer();

    // TODO
    protected boolean isAsync() {
        return true;
    }

    protected boolean isParameterized() {
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }
}
