/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *
 *  This file is part of ReActions.
 *
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.time.wait.WaitTask;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Aliased.Names({"RUN", "EXEC"})
public class ExecuteAction implements Action {
    private final ReActions.Platform platform;
    private final Action functAction;

    public ExecuteAction(@NotNull ReActions.Platform platform) {
        this.platform = platform;
        this.functAction = Objects.requireNonNull(platform.getActivities().getAction("FUNCTION"));
    }

    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        int repeat = Math.max(params.getInteger("repeat", 1), 1);
        long delayMs = TimeUtils.parseTime(params.getString("delay", "0"));
        List<Player> targets = new ArrayList<>();
        if (params.contains("player")) {
            targets.addAll(platform.getSelectors().getPlayerList(Parameters.fromString(params.getString("player"), "player")));
        }
        if (targets.isEmpty()) {
            if (!params.containsAny(ReActions.getSelectors().getAllKeys())) {  // TODO Remove legacy compatibility (selectors)
                targets.add(context.getPlayer());
            } else {
                return false;
            }
        }
        var storedFunct = List.of(new StoredAction(functAction, paramsStr));
        for (int i = 0; i < repeat; i++) {
            for (Player player : targets) {
                platform.getWaiter().schedule(new WaitTask(
                        context.getVariables().fork(),
                        player == null ? null : player.getUniqueId(),
                        storedFunct,
                        delayMs * (i + 1)
                ));
            }
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "EXECUTE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
