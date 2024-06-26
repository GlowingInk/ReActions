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

package fun.reactions.util.time;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtils { // TODO Generalize formatTime/formatIngameTime
    private static final Pattern TIME_SPLITTED = Pattern.compile("(\\d+):(\\d+)(?::(\\d+(?:\\.\\d+(?:[eE]-\\d+)?)?))?");
    private static final Pattern TIME_PRECISE = Pattern.compile("(\\d+)(m[so]|[ywdhmst])?", Pattern.CASE_INSENSITIVE);
    private static final DateFormat DEF_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public static final long MS_PER_SECOND = 1000L;
    public static final long MS_PER_MINUTE = MS_PER_SECOND * 60L;
    public static final long MS_PER_HOUR = MS_PER_MINUTE * 60L;
    public static final long MS_PER_DAY = MS_PER_HOUR * 24L;

    public static final long MS_PER_WEEK = MS_PER_DAY * 7L;
    public static final long MS_PER_MONTH = MS_PER_DAY * 30L;
    public static final long MS_PER_YEAR = MS_PER_DAY * 365L;

    public static final long MS_PER_TICK = MS_PER_SECOND / 20L;

    private TimeUtils() {}

    public static long addOffset(long offsetMs) {
        return System.currentTimeMillis() + offsetMs;
    }

    public static long offsetUntil(long futureTimestamp) {
        return futureTimestamp - System.currentTimeMillis();
    }

    public static long offsetFrom(long pastTimestamp) {
        return System.currentTimeMillis() - pastTimestamp;
    }

    public static String formatIngameTime() {
        return formatIngameTime(Bukkit.getWorlds().get(0));
    }

    public static String formatIngameTime(World world) {
        return formatIngameTime(world.getTime(), false);
    }

    public static String formatIngameTime(long msTime, boolean simpleFormat) {
        return simpleFormat && msTime < MS_PER_SECOND
                ? msTime + "ms"
                : (long) ((msTime / (double) MS_PER_SECOND + 6) % 24) + ":" + (long) (60 * (msTime % (double) MS_PER_SECOND) / MS_PER_SECOND);
    }

    public static String formatTime(long msTime, String format) {
        return formatTime(msTime, new SimpleDateFormat(format));
    }

    public static String formatTime(long msTime) {
        return formatTime(msTime, DEF_FORMAT);
    }

    public static String formatTime(long msTime, DateFormat format) {
        return format.format(new Date(msTime));
    }

    public static long timeToTicksSafe(long msTime) {
        return Math.max(1, timeToTicks(msTime));
    }

    public static long timeToTicks(long msTime) {
        return msTime / MS_PER_TICK;
    }

    public static long parseTime(@NotNull String timeStr) {
        long time = parseTimeSplitted(timeStr);
        return time == -1
                ? parseTimePrecise(timeStr)
                : time;
    }

    public static long parseTimeSplitted(@NotNull String timeStr) {
        Matcher matcher = TIME_SPLITTED.matcher(timeStr);
        if (!matcher.matches()) return -1;
        long time = MS_PER_HOUR * Long.parseLong(matcher.group(1)) + MS_PER_MINUTE * Long.parseLong(matcher.group(2));
        if (matcher.group(3) != null) time += (long) (MS_PER_SECOND * Double.parseDouble(matcher.group(3)));
        return time;
    }

    public static long parseTimePrecise(@NotNull String timeStr) {
        Matcher matcher = TIME_PRECISE.matcher(timeStr);
        long time = 0;
        while (matcher.find()) {
            String unit = matcher.group(2);
            time += Long.parseLong(matcher.group(1)) * switch (unit == null ? "s" : unit.toLowerCase(Locale.ROOT)) {
                case "y" -> MS_PER_YEAR;
                case "mo" -> MS_PER_MONTH;
                case "w" -> MS_PER_WEEK;
                case "d" -> MS_PER_DAY;
                case "h" -> MS_PER_HOUR;
                case "m" -> MS_PER_MINUTE;
                default -> MS_PER_SECOND;
                case "t" -> MS_PER_TICK;
                case "ms" -> 1L;
            };
        }
        return Math.max(time, 0);
    }
}
