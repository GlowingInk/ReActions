package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.time.waiter.WaitingManager;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("ACTDELAY")
public class ActionDelayed implements Action {

    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        long delay = TimeUtils.parseTime(params.getString("time", "0"));
        if (delay == 0) return false;

        String actionSource = params.getString("action");
        if (actionSource.isEmpty()) return false;
        String actionStr;
        String paramStr = "";
        if (!actionSource.contains(" ")) actionStr = actionSource;
        else {
            actionStr = actionSource.substring(0, actionSource.indexOf(" "));
            paramStr = actionSource.substring(actionSource.indexOf(" ") + 1);
        }

        Action action = ReActions.getActivities().getAction(actionStr);
        if (action == null) {
            Msg.logOnce(actionSource, "Failed to execute delayed action: " + actionSource);
            return false;
        }

        StoredAction av = new StoredAction(action, paramStr);
        WaitingManager.schedule(context.getPlayer(), av, delay);
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
