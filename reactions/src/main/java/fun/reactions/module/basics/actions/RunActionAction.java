package fun.reactions.module.basics.actions;

import fun.reactions.ReActions;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.time.wait.WaitTask;
import fun.reactions.util.message.Msg;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static fun.reactions.util.TimeUtils.offsetNow;

@Aliased.Names({"ACTDELAY", "DELAYED_ACTION", "ACTION_DELAYED", "RUNTIME_ACTION"})
public class RunActionAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        Parameters params = Parameters.fromString(content);

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
                    List.of(new Stored(action, actionParamsStr)),
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
