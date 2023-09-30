package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.actions.Interrupting;
import fun.reactions.model.environment.Environment;
import fun.reactions.time.wait.WaitTask;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static fun.reactions.util.TimeUtils.offsetNow;

@Aliased.Names("PAUSE")
public class WaitAction implements Action, Interrupting {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "WAIT";
    }

    @Override
    public void stop(@NotNull Environment env, @NotNull String params, @NotNull List<Stored> remaining) {
        env.getPlatform().getWaiter().schedule(new WaitTask(
                env.getVariables().fork(),
                env.getPlayer() != null ? env.getPlayer().getUniqueId() : null,
                remaining,
                offsetNow(Parameters.fromString(params, "time").getTime("time", 0))
        ));
    }
}
