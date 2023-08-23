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

package fun.reactions.module.basics.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.TimeUtils;
import fun.reactions.util.message.Msg;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Aliased.Names("MSG")
@Deprecated
public class MessageAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        sendMessage(env, env.getPlayer(), params);
        return true;
    }

    private static void sendMessage(Environment env, Player player, Parameters params) {
        Set<Player> players = new HashSet<>();
        if (params.containsAny(env.getPlatform().getSelectors().getAllKeys())) {
            players.addAll(env.getPlatform().getSelectors().getPlayerList(params));
            if (players.isEmpty() && params.contains("player")) {
                players.addAll(env.getPlatform().getSelectors().getPlayerList(Parameters.fromString(params.getString("player"))));
            }
        } else if (player != null) {
            players.add(player);
        }
        if (players.isEmpty()) return;

        String type = params.getString("type");
        String message = Msg.colorize(params.getStringSafe("text", () -> hideSelectors(env, params.origin())));
        String annoymentTime = params.getString("hide");
        for (Player receiver : players) {
            if (showMessage(env, receiver, message, annoymentTime)) {
                switch (type.toLowerCase(Locale.ROOT)) {
                    case "title" -> receiver.sendTitle(
                            message,
                            Msg.colorize(params.getString("subtitle", null)),
                            params.getInteger("fadein", 10),
                            params.getInteger("stay", 70),
                            params.getInteger("fadeout", 20)
                    );
                    case "subtitle" -> receiver.sendTitle(
                            null,
                            message,
                            params.getInteger("fadein", 10),
                            params.getInteger("stay", 70),
                            params.getInteger("fadeout", 20)
                    );
                    case "actionbar" -> receiver.sendActionBar(message);
                    default -> receiver.sendMessage(message);
                }
            }
        }
    }

    private static boolean showMessage(Environment env, Player player, String message, String annoymentTime) {
        if (annoymentTime.isEmpty()) return true;
        long time = TimeUtils.parseTime(annoymentTime);
        if (time == 0) return false;
        String key = "reactions-msg-" +/*.append(this.getActivatorName())*/message.hashCode();
        if (player.hasMetadata(key)) {
            if ((player.getMetadata(key).get(0).asLong() - System.currentTimeMillis()) > 0)
                return false;
        }
        player.setMetadata(key, new FixedMetadataValue(env.getPlatform().getPlugin(), System.currentTimeMillis() + time));
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "MESSAGE";
    }

    private static String hideSelectors(Environment env, String message) {
        String sb = "(?i)(" + String.join("|", env.getPlatform().getSelectors().getAllKeys()) + "|type|hide):(\\{.*}|\\S+)\\s?";
        return message.replaceAll(sb, "");
    }
}
