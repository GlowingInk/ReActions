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

package me.fromgate.reactions.util.message;

import me.fromgate.reactions.Cfg;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.ChatPaginator;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BukkitMessenger implements Messenger {

    private final JavaPlugin plugin;
    private final DecimalFormat TWO_DECIMALS = new DecimalFormat("####0.##");

    public BukkitMessenger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static void printPage(CommandSender sender, List<String> list, Msg title, int page) {
        int pageHeight = (sender instanceof Player) ? 9 : 1000;
        if (title != null) title.print(sender);
        ChatPaginator.ChatPage chatPage = paginate(list, page, Cfg.chatLength, pageHeight);
        for (String str : chatPage.getLines()) {
            Msg.printMessage(sender, str);
        }

        if (pageHeight == 9) {
            Msg.LST_FOOTER.print(sender, 'e', '6', chatPage.getPageNumber(), chatPage.getTotalPages());
        }
    }

    public static ChatPaginator.ChatPage paginate(List<String> unpaginatedStrings, int pageNumber, int lineLength, int pageHeight) {
        List<String> lines = new ArrayList<>();
        for (String str : unpaginatedStrings) {
            lines.addAll(Arrays.asList(ChatPaginator.wordWrap(str, lineLength)));
        }
        int totalPages = lines.size() / pageHeight + (lines.size() % pageHeight == 0 ? 0 : 1);
        int actualPageNumber = Math.min(pageNumber, totalPages);
        int from = (actualPageNumber - 1) * pageHeight;
        int to = Math.min(from + pageHeight, lines.size());
        String[] selectedLines = Arrays.copyOfRange(lines.toArray(new String[0]), from, to);
        return new ChatPaginator.ChatPage(selectedLines, actualPageNumber, totalPages);
    }

    @Override
    public String colorize(String text) {
        return text != null ? ChatColor.translateAlternateColorCodes('&', text) : text;
    }

    @Override
    public boolean log(String text) {
        plugin.getLogger().info(text);
        return true;
    }

    @Override
    public String clean(String text) {
        return ChatColor.stripColor(text);
    }

    @Override
    public boolean print(Object obj, String text) {
        CommandSender sender = toSender(obj);
        if (sender != null) {
            sender.sendMessage(text);
        } else {
            log("Failed to print message - wrong recipient: " + (obj == null ? "null" : obj.toString()));
        }
        return true;
    }

    @Override
    public String toString(Object obj, boolean fullFloat) {
        if (obj == null) return "'null'";
        String s = obj.toString();
        if (obj instanceof Location loc) {
            if (fullFloat)
                s = loc.getWorld() + "[" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + "]";
            else
                s = loc.getWorld() + "[" + TWO_DECIMALS.format(loc.getX()) + ", " + TWO_DECIMALS.format(loc.getY()) + ", " + TWO_DECIMALS.format(loc.getZ()) + "]";
        }
        return s;
    }

    @Override
    public Map<String, String> load(String language) {
        Map<String, String> msg = new HashMap<>();
        YamlConfiguration lng = new YamlConfiguration();
        File f = new File(plugin.getDataFolder() + File.separator + language + ".lng");
        try {
            if (f.exists()) lng.load(f);
            else {
                InputStream is = plugin.getClass().getResourceAsStream("/language/" + language + ".lng");
                if (is != null) lng.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            Msg.LNG_LOAD_FAIL.log();
            return msg;
        }

        for (String key : lng.getKeys(true)) {
            if (lng.isConfigurationSection(key)) continue;
            msg.put(key, lng.getString(key));
        }
        return msg;
    }

    @Override
    public void save(String language, Map<String, String> messages) {
        YamlConfiguration lng = new YamlConfiguration();
        File f = new File(plugin.getDataFolder() + File.separator + language + ".lng");
        try {
            if (f.exists()) lng.load(f);
        } catch (Exception ignore) {
        }

        for (Map.Entry<String, String> message : messages.entrySet())
            lng.set(message.getKey().toLowerCase(Locale.ENGLISH), message.getValue());

        try {
            lng.save(f);
        } catch (Exception e) {
            Msg.LNG_SAVE_FAIL.log();
        }
    }

    @Override
    public boolean isValidSender(Object send) {
        return (toSender(send) != null);
    }

    private CommandSender toSender(Object sender) {
        return sender instanceof CommandSender ? (CommandSender) sender : null;
    }
}
