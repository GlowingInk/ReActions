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

package fun.reactions.util.message;

import fun.reactions.Cfg;
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
import java.util.*;

@Deprecated
public class Messenger {
    private final JavaPlugin plugin;
    private final DecimalFormat TWO_DECIMALS = new DecimalFormat("####0.##");

    public Messenger(JavaPlugin plugin) {
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

    public String colorize(String text) {
        return text != null ? ChatColor.translateAlternateColorCodes('&', text) : text;
    }

    public void log(String text) {
        plugin.getLogger().info(text);
    }

    public String clean(String text) {
        return ChatColor.stripColor(text);
    }

    public void print(CommandSender sender, String text) {
        if (sender != null) {
            sender.sendMessage(text);
        } else {
            log("Failed to print message - wrong recipient");
        }
    }

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

    public void save(String language, Map<String, String> messages) {
        YamlConfiguration lng = new YamlConfiguration();
        File f = new File(plugin.getDataFolder() + File.separator + language + ".lng");
        try {
            if (f.exists()) lng.load(f);
        } catch (Exception ignore) {
        }

        for (Map.Entry<String, String> message : messages.entrySet())
            lng.set(message.getKey().toLowerCase(Locale.ROOT), message.getValue());

        try {
            lng.save(f);
        } catch (Exception e) {
            Msg.LNG_SAVE_FAIL.log();
        }
    }
}
