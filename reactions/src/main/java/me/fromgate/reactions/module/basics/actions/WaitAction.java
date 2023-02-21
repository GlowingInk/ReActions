package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.Stopper;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.time.wait.WaitTask;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.fromgate.reactions.util.TimeUtils.offsetNow;
import static me.fromgate.reactions.util.TimeUtils.parseTime;

@Aliased.Names("PAUSE")
public class WaitAction implements Action, Stopper {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr, "time");
        long time = parseTime(params.getString("time", "0"));
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
    public void stop(@NotNull Environment env, @NotNull String params, @NotNull List<StoredAction> actions) { // TODO Append variables
        ReActions.getWaiter().schedule(new WaitTask(
                env.getVariables().fork(),
                env.getPlayer() != null ? env.getPlayer().getUniqueId() : null,
                actions,
                offsetNow(parseTime(Parameters.fromString(params, "time").getString("time", "1")))
        ));
    }
}
