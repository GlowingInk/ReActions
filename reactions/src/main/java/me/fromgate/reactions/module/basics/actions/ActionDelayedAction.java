package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"ACTDELAY", "DELAYED_ACTION"})
public class ActionDelayedAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        long delay = TimeUtils.parseTime(params.getString("time", "1"));
        if (delay == 0) return false;

        String actionSource = params.getString("action");
        if (actionSource.isEmpty()) return false;
        String actionStr;
        String paramStr = "";
        int splitIndex = actionSource.indexOf(' ');
        if (splitIndex == -1) {
            actionStr = actionSource;
        } else {
            actionStr = actionSource.substring(0, splitIndex);
            paramStr = actionSource.substring(splitIndex + 1);
        }

        Action action = ReActions.getActivities().getAction(actionStr);
        if (action == null) {
            Msg.logOnce(actionSource, "Failed to execute delayed action: " + actionSource);
            return false;
        }

        StoredAction av = new StoredAction(action, paramStr);
        ReActions.getWaiter().schedule(av, delay);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "ACTION_DELAYED";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
