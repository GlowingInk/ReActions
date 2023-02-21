package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.time.wait.WaitTask;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.fromgate.reactions.util.TimeUtils.offsetNow;

@Aliased.Names({"ACTDELAY", "DELAYED_ACTION", "ACTION_DELAYED", "RUNTIME_ACTION"})
public class RunActionAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);

        long delayMs = params.getTime(params.findKey("delay", "time"));

        String actionStr;
        String actionParamsStr;
        if (params.contains("action")) {
            String actionSource = params.getString("action");
            int splitIndex = actionSource.indexOf(' ');
            if (splitIndex == -1) {
                actionStr = actionSource;
                actionParamsStr = "";
            } else {
                actionStr = actionSource.substring(0, splitIndex);
                actionParamsStr = actionSource.substring(splitIndex + 1);
            }
        } else {
            actionStr = params.getString("type");
            actionParamsStr = params.getString("params");
        }

        Action action = ReActions.getActivities().getAction(actionStr);
        if (action == null) { // TODO Replace with some no-such-action message
            Msg.logOnce(actionStr, "Failed to execute delayed action: '" + actionStr + "'");
            return false;
        }

        if (delayMs <= 0) {
            if (!action.requiresPlayer() || env.getPlayer() != null) {
                action.proceed(env, actionParamsStr);
            }
        } else {
            ReActions.getWaiter().schedule(new WaitTask(
                    env.getVariables(),
                    env.getPlayer() != null ? env.getPlayer().getUniqueId() : null,
                    List.of(new StoredAction(action, actionParamsStr)),
                    offsetNow(delayMs)
            ));
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "RUN_ACTION";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
