package fun.reactions.module.basics.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("FLIGHT")
public class FlyAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = env.getPlayer();
        if (params.contains("player"))
            player = Bukkit.getPlayerExact(params.getString("player"));
        AllowFly allow = AllowFly.NONE;
        AllowFly fly = AllowFly.NONE;
        if (params.contains("allow")) {
            if (params.getBoolean("allow", true)) allow = AllowFly.TRUE;
            else allow = AllowFly.FALSE;
        }
        if (params.contains("fly")) {
            if (params.getBoolean("fly", true)) fly = AllowFly.TRUE;
            else fly = AllowFly.FALSE;
        }

        return flyPlayer(player, allow, fly);
    }

    @Override
    public @NotNull String getName() {
        return "FLY";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    private boolean flyPlayer(Player player, AllowFly allow, AllowFly fly) {
        if (player == null || player.isDead() || !player.isOnline()) return false;

        if (allow == AllowFly.TRUE && fly == AllowFly.TRUE || allow == AllowFly.FALSE && fly == AllowFly.FALSE) {
            player.setAllowFlight(allow == AllowFly.TRUE);
            player.setFlying(fly == AllowFly.TRUE);
        }
        if (allow == AllowFly.TRUE && fly == AllowFly.FALSE) {
            if (!player.getAllowFlight()) player.setAllowFlight(true);
            if (player.isFlying()) player.setFlying(false);
        }
        if (allow == AllowFly.FALSE && fly == AllowFly.TRUE) {
            if (!player.isFlying()) player.setAllowFlight(false);
        }
        if (allow == AllowFly.TRUE && fly == AllowFly.NONE) {
            if (!player.getAllowFlight()) player.setAllowFlight(true);
        }
        if (allow == AllowFly.FALSE && fly == AllowFly.NONE) {
            if (player.getAllowFlight()) player.setAllowFlight(false);
        }
        if (allow == AllowFly.NONE && fly == AllowFly.TRUE) {
            if (!player.getAllowFlight()) player.setAllowFlight(true);
            player.setFlying(true);
        }
        if (allow == AllowFly.NONE && fly == AllowFly.FALSE) {
            player.setFlying(false);
        }

        return allow != AllowFly.NONE && fly != AllowFly.NONE;
    }

    private enum AllowFly {
        TRUE,
        FALSE,
        NONE
    }
}
