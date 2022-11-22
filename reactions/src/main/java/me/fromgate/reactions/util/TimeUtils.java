/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *   *
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

package me.fromgate.reactions.util;

import org.bukkit.Bukkit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtils {
    private static final Pattern TIME_SPLITTED = Pattern.compile("(\\d+):(\\d+)(?::(\\d+))?");
    private static final Pattern TIME_PRECISE = Pattern.compile("(\\d+)([dhmst]|ms)");
    private static final DateFormat DEF_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private TimeUtils() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static String formattedIngameTime() {
        return formattedIngameTime(Bukkit.getWorlds().get(0).getTime(), false);
    }

    public static String formattedIngameTime(long time, boolean showms) {
        return showms && (time < 1000) ?
                time + "ms" :
                (long) ((time / 1000D + 6) % 24) + ":" + (long) (60 * (time % 1000D) / 1000);
    }

    public static String fullTimeToString(long time, DateFormat format) {
        return format.format(new Date(time));
    }

    public static String fullTimeToString(long time, String format) {
        return fullTimeToString(time, new SimpleDateFormat(format));
    }

    public static String fullTimeToString(long time) {
        return fullTimeToString(time, DEF_FORMAT);
    }

    public static long timeToTicks(long time) {
        // 1000 ms = 20 ticks; 50 ms = 1 tick
        return Math.max(1, (time / 50));
    }

    public static long parseTime(String timeStr) {
        if (NumberUtils.isPositiveInt(timeStr)) {
            return Long.parseLong(timeStr) * 1000L;
        }
        Matcher matcher = TIME_SPLITTED.matcher(timeStr);
        if (matcher.matches()) {
            long time = 3600000L * Long.parseLong(matcher.group(1)) + 60000L * Long.parseLong(matcher.group(2));
            if (matcher.groupCount() == 3) time += 1000L * Long.parseLong(matcher.group(3));
            return time;
        }
        matcher = TIME_PRECISE.matcher(timeStr);
        long time = 0;
        while (matcher.find()) {
            time += Long.parseLong(matcher.group(1)) * switch (matcher.group(2)) {
                case "d" -> 86400000L;
                case "h" -> 3600000L;
                case "m" -> 60000L;
                default -> 1000L; // seconds
                case "t" -> 50L;
                case "ms" -> 1L;
            };
        }
        return Math.max(time, 1);
    }
}
