package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.Stopper;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ActionWait implements Action, Stopper {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
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
    public void stop(@NotNull Environment context, @NotNull String params, @NotNull List<StoredAction> actions) {
        ReActions.getWaiter().schedule(
                Optional.ofNullable(context.getPlayer()).map(Player::getUniqueId).orElse(null),
                actions,
                TimeUtils.parseTime(Parameters.fromString(params, "time").getString("time", "1"))
        );
    }
}
