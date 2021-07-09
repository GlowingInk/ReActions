package me.fromgate.reactions.module.defaults.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.time.waiter.WaitingManager;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Alias("ACTDELAY")
public class ActionDelayed extends Action {

    // TODO Make it actually work
    @Override
    protected boolean execute(@NotNull RaContext context, @NotNull Parameters params) {
        long delay = TimeUtils.parseTime(params.getString("time", "0"));
        if (delay == 0) return false;

        String actionSource = params.getString("action", "");
        if (actionSource.isEmpty()) return false;
        String actionStr;
        String paramStr = "";
        if (!actionSource.contains(" ")) actionStr = actionSource;
        else {
            actionStr = actionSource.substring(0, actionSource.indexOf(" "));
            paramStr = actionSource.substring(actionSource.indexOf(" ") + 1);
        }

        if (!Actions.isValid(actionStr)) {
            Msg.logOnce(actionSource, "Failed to execute delayed action: " + actionSource);
            return false;
        }

        StoredAction av = new StoredAction(actionStr, paramStr);
        WaitingManager.executeDelayed(context.getPlayer(), av, this.isAction(), delay);
        return false;
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
