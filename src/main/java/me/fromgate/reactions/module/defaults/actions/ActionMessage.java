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

package me.fromgate.reactions.module.defaults.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.selectors.SelectorsManager;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Alias("MSG")
public class ActionMessage extends Action {

    @Override
    protected boolean execute(@NotNull RaContext context, @NotNull Parameters params) {
        sendMessage(context.getPlayer(), params);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "MESSAGE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    // TODO: Remove it somehow
    private static String removeParams(String message) {
        String sb = "(?i)(" + String.join("|", SelectorsManager.getAllKeys()) + "|type|hide):(\\{.*}|\\S+)\\s?";
        return message.replaceAll(sb, "");

    }

    private static void sendMessage(Player player, Parameters params) {
        Set<Player> players = new HashSet<>();
        if (params.containsAny(SelectorsManager.getAllKeys())) {
            players.addAll(SelectorsManager.getPlayerList(params));
            if (players.isEmpty() && params.contains("player")) {
                players.addAll(SelectorsManager.getPlayerList(Parameters.fromString(params.getString("player"))));
            }
        } else if (player != null) {
            players.add(player);
        }
        if (players.isEmpty()) return;

        String type = params.getString("type", "");
        String message = params.getString("text", removeParams(params.toString()));
        if (message.isEmpty()) return;
        String annoymentTime = params.getString("hide");
        for (Player p : players) {
            if (showMessage(p, message, annoymentTime)) {
                switch (type.toLowerCase(Locale.ENGLISH)) {
                    case "title" -> p.sendTitle(Msg.colorize(message),
                            params.getString("subtitle", null),
                            params.getInteger("fadein", 10),
                            params.getInteger("stay", 70),
                            params.getInteger("fadeout", 20)
                    );
                    case "subtitle" -> p.sendTitle(null,
                            Msg.colorize(params.getString("subtitle", null)),
                            params.getInteger("fadein", 10),
                            params.getInteger("stay", 70),
                            params.getInteger("fadeout", 20)
                    );
                    case "actionbar" -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Msg.colorize(message)));
                    default -> Msg.printMessage(p, message);
                }
            }
        }
    }

    private static boolean showMessage(Player player, String message, String annoymentTime) {
        if (annoymentTime.isEmpty()) return true;
        long time = TimeUtils.parseTime(annoymentTime);
        if (time == 0) return false;
        String key = "reactions-msg-" +/*.append(this.getActivatorName())*/message.hashCode();
        if (player.hasMetadata(key)) {
            if ((player.getMetadata(key).get(0).asLong() - System.currentTimeMillis()) > 0)
                return false;
        }
        player.setMetadata(key, new FixedMetadataValue(ReActions.getPlugin(), System.currentTimeMillis() + time));
        return true;
    }
}
