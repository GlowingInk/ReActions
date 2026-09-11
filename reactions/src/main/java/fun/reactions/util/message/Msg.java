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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public enum Msg { // TODO Isn't really needed
    MSG_VARLIST("Variables"),
    MSG_SIGNFORBIDDEN("You're not permitted to set signs, that subscribed to activator %1%"),
    MSG_MOBBOUNTY("You received %1% for killing %2%"),
    MSG_TIMERLIST("Timers"),
    MSG_TIMERNEEDNAME("You must define name for the timer"),
    MSG_TIMERUNKNOWNNAME("Could not find timer %1%"),
    MSG_TIMERREMOVED("Timer %1% removed"),
    LNG_CONFIG("[MESSAGES] Messages: %1% Language: %2% Save translate file: %1% Debug mode: %3%"),
    LNG_PRINT_FAIL_M("Failed to print message. Unknown key %1%"),
    LNG_PRINT_FAIL("Failed to print message %1%. Sender object is null.");

    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("####0.##");

    private static JavaPlugin plugin;

    private static boolean debugMode = false;
    private static final Set<String> onceLog = new HashSet<>();
    private final String message;
    private final Character color1;
    private final Character color2;

    Msg(String msg) {
        message = msg;
        this.color1 = null;
        this.color2 = null;
    }

    Msg(String msg, char color1, char color2) {
        this.message = msg;
        this.color1 = color1;
        this.color2 = color2;
    }

    Msg(String msg, char color) {
        this(msg, color, color);
    }

    public static String colorize(String text) {
        return text != null ? ChatColor.translateAlternateColorCodes('&', text) : text;
    }

    public static String clean(String str) {
        return ChatColor.stripColor(str);
    }

    /**
     * Initialize current class, load messages, etc.
     * Call this file in onEnable method after initializing plugin configuration
     */
    public static void init(JavaPlugin plugin, boolean debug) {
        Msg.plugin = plugin;
        debugMode = debug;
        LNG_CONFIG.debug(Msg.values().length, true, debugMode);
    }

    private static void log(String text) {
        plugin.getLogger().info(text);
    }

    private static String toString(Object obj, boolean fullFloat) {
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

    /**
     * Join object array to string (separated by space)
     *
     * @param s - array of any object that you need to join.
     */
    private static String join(Object... s) {
        StringBuilder sb = new StringBuilder();
        for (Object o : s) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(toString(o, false));
        }
        return sb.toString();
    }

    public static boolean logMessage(Object... s) {
        if (s == null) return false;
        log(clean(join(s)));
        return true;
    }

    public static Msg getByName(String name) {
        for (Msg m : values()) {
            if (m.name().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public static void logOnce(String key, Object... s) {
        if (onceLog.contains(key)) return;
        onceLog.add(key);
        Msg.logMessage(s);
    }

    /**
     * Send current message to log files
     *
     * @param s - array of any object that you need to print out.
     * @return — always returns true.
     * Examples:
     * Message.ERROR_MESSAGE.log(variable1); // just print in log
     * return Message.ERROR_MESSAGE.log(variable1); // print in log and return value true
     */
    public void log(Object... s) {
        Msg.logMessage(getText(s));
    }

    /**
     * Same as log, but will printout nothing if debug mode is disabled
     *
     * @param s - array of any object that you need to print out.
     * @return — always returns true.
     */
    public void debug(Object... s) {
        if (debugMode) {
            log(clean(getText(s)));
        }
    }

    /**
     * Send message to Player or to ConsoleSender
     *
     * @param sender
     * @param s      - array of any object that you need to print out.
     * @return — always returns true.
     */
    public void print(CommandSender sender, Object... s) {
        if (sender == null) {
            log(this.name());
        } else {
            sender.sendMessage(getText(s));
        }
    }

    /**
     * Get formated text.
     *
     * @param keys * Keys - are parameters for message and control-codes.
     *             Parameters will be shown in position in original message according for position.
     *             This keys are used in every method that prints or sends message.
     *             <p>
     *             Example:
     *             <p>
     *             EXAMPLE_MESSAGE ("Message with parameters: %1%, %2% and %3%");
     *             Message.EXAMPLE_MESSAGE.getText("one","two","three"); //will return text "Message with parameters: one, two and three"
     *             <p>
     *             * Color codes
     *             You can use two colors to define color of message, just use character symbol related for color.
     *             <p>
     *             Message.EXAMPLE_MESSAGE.getText("one","two","three",'c','4');  // this message will be red, but word one, two, three - dark red
     *             <p>
     *             * Control codes
     *             Control codes are text parameteres, that will be ignored and don't shown as ordinary parameter
     *             - "SKIPCOLOR" - use this to disable colorizing of parameters
     *             - "NOCOLOR" (or "NOCOLORS") - return uncolored text, clear all colors in text
     *             - "FULLFLOAT" - show full float number, by default it limit by two symbols after point (0.15 instead of 0.1483294829)
     * @return
     */
    public String getText(Object... keys) {
        char c2 = '2';
        char c1 = 'a';
        char[] colors = new char[]{color1 == null ? c1 : color1, color2 == null ? c2 : color2};
        if (keys.length == 0) {
            return colorize("&" + colors[0] + this.message);
        }
        String str = this.message;
        boolean noColors = false;
        boolean skipDefaultColors = false;
        boolean fullFloat = false;
        String prefix = "";
        int count = 1;
        int c = 0;
        DecimalFormat fmt = new DecimalFormat("####0.##");
        for (Object key : keys) {
            String s = toString(key, fullFloat);//keys[i].toString();
            if (c < 2 && key instanceof Character) {
                colors[c] = (Character) key;
                c++;
                continue;
            } else if (s.startsWith("prefix:")) {
                prefix = s.replace("prefix:", "");
                continue;
            } else if (s.equals("SKIPCOLOR")) {
                skipDefaultColors = true;
                continue;
            } else if (s.equals("NOCOLORS") || s.equals("NOCOLOR")) {
                noColors = true;
                continue;
            } else if (s.equals("FULLFLOAT")) {
                fullFloat = true;
                continue;
            } else if (key instanceof Double) {
                if (!fullFloat) s = fmt.format(key);
            } else if (key instanceof Float) {
                if (!fullFloat) s = fmt.format(key);
            }

            String from = "%" + count + "%";
            String to = skipDefaultColors ? s : "&" + colors[1] + s + "&" + colors[0];
            str = str.replace(from, to);
            count++;
        }
        str = colorize(prefix.isEmpty() ? "&" + colors[0] + str : prefix + " " + "&" + colors[0] + str);
        if (noColors) str = clean(str);
        return str;
    }

    @Override
    public String toString() {
        return this.getText("NOCOLOR");
    }
}
