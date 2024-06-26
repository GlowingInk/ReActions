package fun.reactions.module.basic.actions;

import fun.reactions.ReActions;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.time.wait.WaitTask;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static fun.reactions.util.time.TimeUtils.addOffset;

@Aliased.Names({
        "ACTDELAY", "DELAYED_ACTION", "ACTION_DELAYED",
        "RUNTIME_ACTION", "RUN_ACTION",
        "DYNACTION", "DYN_ACTION"
})
public class DynamicActionAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);

        long delayMs = params.getTime(params.findKey("delay", "time"));

        String typeStr;
        String content;
        if (params.contains("action")) {
            String actionSource = params.getString("action");
            int splitIndex = actionSource.indexOf(' ');
            if (splitIndex == -1) {
                typeStr = actionSource;
                content = "";
            } else {
                typeStr = actionSource.substring(0, splitIndex);
                content = actionSource.substring(splitIndex + 1);
            }
        } else {
            typeStr = params.getString("type");
            content = params.getString("content");
        }

        ReActions.Platform platform = env.getPlatform();
        Action action = platform.getActivities().getAction(typeStr);
        if (action == null) {
            platform.logger().warn("Failed to prepare dynamic action: action type '" + typeStr + "' doesn't exist");
            return false;
        }

        if (delayMs <= 0) {
            action.proceed(env, content);
        } else {
            platform.getWaiter().schedule(new WaitTask(
                    env.getVariables(),
                    env.getPlayer() != null ? env.getPlayer().getUniqueId() : null, // TODO Selector
                    List.of(new Stored(action, content)),
                    addOffset(delayMs)
            ));
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "DYNAMIC_ACTION";
    }
}
