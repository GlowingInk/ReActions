package me.fromgate.reactions.module.defaults.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.Stopper;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.time.waiter.WaitingManager;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ActionWait extends Action implements Stopper {

    // TODO Make it actually work...

    @Override
    protected boolean execute(@NotNull RaContext context, @NotNull Parameters params) {
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

    public void executeDelayed(Player player, final List<StoredAction> actions, final boolean isAction, long time) {
        if (actions.isEmpty()) return;
        WaitingManager.executeDelayed(player, actions, isAction, time);

		/*final String playerStr = player!=null? player.getName() : null;
		Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), new Runnable(){
			@Override
			public void run() {
				@SuppressWarnings("deprecation")
				Player p = playerStr==null ? null : Util.getPlayerExact(playerStr);
				if (p==null&& playerStr!=null) return;
				Actions.executeActions(p, actions, isAction);
			}
		}, time); */

    }

    @Override
    public void stop(@NotNull RaContext context, @NotNull Parameters params, @NotNull List<StoredAction> actions) {
        WaitingManager.executeDelayed(context.getPlayer(), actions, TimeUtils.parseTime(params.getString("time", "0")));
    }
}
