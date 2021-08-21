package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.Stopper;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.time.waiter.WaitingManager;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ActionWait extends Action implements Stopper {

    // TODO Make it actually work...

    @Override
    protected boolean execute(@NotNull RaContext context, @NotNull Parameters params) {
        long time = TimeUtils.parseTime(params.getString("time", "0"));
        return time > 0;
    }

    @Override
    public @NotNull String getName() {
        return "WAIT";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    public void stop(@NotNull RaContext context, @NotNull String params, @NotNull List<StoredAction> actions) {
        WaitingManager.executeDelayed(context.getPlayer(), actions, TimeUtils.parseTime(Parameters.fromString(params, "time").getString("time", "1")));
    }
}
