package fun.reactions.module.basics.actions;

import fun.reactions.ReActions;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.Utils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 5/6/2017.
 */
public class PlayerIdAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        String uuid;
        String pName;

        Parameters params = Parameters.fromString(content);
        String playerParam = params.getString("player");

        if (Utils.isStringEmpty(playerParam)) {
            uuid = env.getPlayer().getUniqueId().toString();
            pName = env.getPlayer().getName();
        } else {
            OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerParam);
            uuid = offPlayer.getUniqueId().toString();
            pName = offPlayer.getName();
            if (pName == null)
                pName = "";

            String varID = params.getString("varid");
            if (!Utils.isStringEmpty(varID))
                ReActions.getVariables().setVariable(playerParam, varID, uuid);
            String varName = params.getString("varname");
            if (!Utils.isStringEmpty(varName))
                ReActions.getVariables().setVariable(playerParam, varName, pName);
        }

        env.getVariables().set("playerid", uuid);
        env.getVariables().set("playername", pName);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "PLAYER_ID";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

}
