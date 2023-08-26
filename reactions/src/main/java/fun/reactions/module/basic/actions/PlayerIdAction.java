package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.Utils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author MaxDikiy
 * @since 05/06/2017
 */
public class PlayerIdAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        String uuid;
        String pName;

        Parameters params = Parameters.fromString(paramsStr);
        String playerParam = params.getString("player");

        if (Utils.isStringEmpty(playerParam)) {
            Player player = env.getPlayer();
            if (player == null) return false;
            uuid = player.getUniqueId().toString();
            pName = player.getName();
        } else {
            OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerParam);
            uuid = offPlayer.getUniqueId().toString();
            pName = offPlayer.getName();
            if (pName == null)
                pName = "";

            String varID = params.getString("varid");
            if (!Utils.isStringEmpty(varID))
                env.getPlatform().getPersistentVariables().setVariable(playerParam, varID, uuid);
            String varName = params.getString("varname");
            if (!Utils.isStringEmpty(varName))
                env.getPlatform().getPersistentVariables().setVariable(playerParam, varName, pName);
        }

        env.getVariables().set("playerid", uuid);
        env.getVariables().set("playername", pName);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "PLAYER_ID";
    }

}
