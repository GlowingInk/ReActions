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

package fun.reactions.module.basic.actions;

import fun.reactions.ReActions;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.time.wait.WaitTask;
import fun.reactions.time.wait.WaitingManager;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static fun.reactions.util.TimeUtils.offsetNow;

@Aliased.Names({"RUN", "EXEC"})
public class ExecuteAction implements Action {
    private final RunFunctionAction functAction;

    public ExecuteAction(@NotNull RunFunctionAction functAction) {
        this.functAction = functAction;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        ReActions.Platform platform = env.getPlatform();
        Parameters params = Parameters.fromString(paramsStr);
        int repeat = Math.max(params.getInteger("repeat", 1), 1);
        long delayMs = params.getTime("delay", 0);
        List<Player> targets = new ArrayList<>();
        if (params.contains("player")) {
            targets.addAll(platform.getSelectors().getPlayerList(Parameters.fromString(params.getString("player"), "player")));
        }
        if (targets.isEmpty()) {
            if (!params.containsAny(env.getPlatform().getSelectors().getAllKeys())) {  // TODO Remove legacy compatibility (selectors)
                targets.add(env.getPlayer());
            } else {
                return false;
            }
        }
        var storedFunct = List.of(new Stored(functAction, paramsStr));
        WaitingManager waiter = platform.getWaiter();
        for (int i = 0; i < repeat; i++) {
            long until = offsetNow(delayMs * (i + 1));
            for (Player player : targets) {
                waiter.schedule(new WaitTask(
                        env.getVariables().fork(),
                        player == null ? null : player.getUniqueId(),
                        storedFunct,
                        until
                ));
            }
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "EXECUTE";
    }
}
