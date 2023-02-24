package fun.reactions.module.basics.actions;

import fun.reactions.ReActions;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.actions.Stopper;
import fun.reactions.model.environment.Environment;
import fun.reactions.time.wait.WaitTask;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static fun.reactions.util.TimeUtils.offsetNow;
import static fun.reactions.util.TimeUtils.parseTime;

@Aliased.Names("PAUSE")
public class WaitAction implements Action, Stopper {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        Parameters params = Parameters.fromString(content, "time");
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
    public void stop(@NotNull Environment env, @NotNull String params, @NotNull List<Stored> actions) { // TODO Append variables
        ReActions.getWaiter().schedule(new WaitTask(
                env.getVariables().fork(),
                env.getPlayer() != null ? env.getPlayer().getUniqueId() : null,
                actions,
                offsetNow(parseTime(Parameters.fromString(params, "time").getString("time", "1")))
        ));
    }
}
