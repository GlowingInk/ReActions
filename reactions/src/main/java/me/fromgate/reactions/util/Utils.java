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

package me.fromgate.reactions.util;

import me.fromgate.reactions.util.alias.Aliased;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.fromgate.reactions.util.NumberUtils.getInteger;

public final class Utils {
    private static final Pattern HEX_COLOR = Pattern.compile("#([a-fA-F\\d]{6})");
    private static final Pattern BYTE_COLOR = Pattern.compile("(\\d{1,3}),(\\d{1,3}),(\\d{1,3})");
    private static final String[] EMPTY_ARRAY = new String[0];

    private Utils() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static String removeChar(String str, char remove) {
        StringBuilder bld = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c != remove) bld.append(c);
        }
        return bld.toString();
    }

    /**
     * Play sound on location
     *
     * @param loc    Location where sound should be played
     * @param params Parameters of sound
     * @return Name of played sound
     */
    public static String soundPlay(Location loc, Parameters params) {
        if (params.isEmpty()) return "";
        Location soundLoc = loc;
        String sndstr = "";
        String strvolume = "1";
        String strpitch = "1";
        float pitch = 1;
        float volume = 1;
        if (params.contains("param")) {
            String param = params.getString("param", "");
            if (param.isEmpty()) return "";
            if (param.contains("/")) {
                String[] prm = param.split("/");
                if (prm.length > 1) {
                    sndstr = prm[0];
                    strvolume = prm[1];
                    if (prm.length > 2) strpitch = prm[2];
                }
            } else sndstr = param;
            if (NumberUtils.FLOAT_POSITIVE.matcher(strvolume).matches()) volume = Float.parseFloat(strvolume);
            if (NumberUtils.FLOAT_POSITIVE.matcher(strpitch).matches()) pitch = Float.parseFloat(strpitch);
        } else {
            String locationStr = params.getString("loc");
            soundLoc = locationStr.isEmpty() ? loc : LocationUtils.parseLocation(locationStr, null);
            sndstr = params.getString("type", "");
            pitch = (float) params.getDouble("pitch", 1.0f);
            volume = (float) params.getDouble("volume", 1.0f);
        }
        Sound sound = getEnum(Sound.class, sndstr, Sound.UI_BUTTON_CLICK);
        if (soundLoc != null) soundLoc.getWorld().playSound(soundLoc, sound, volume, pitch);
        return sound.name();
    }

    /**
     * Play sound on location
     *
     * @param loc   Location where sound should be played
     * @param param Parameters of sound
     */
    public static void soundPlay(Location loc, String param) {
        if (param.isEmpty()) return;
        Parameters params = Parameters.fromString(param, "param");
        soundPlay(loc, params);
    }

    /**
     * Check string is empty or null
     *
     * @param str String to check
     * @return Is string empty or null
     */
    @Contract("null -> true")
    public static boolean isStringEmpty(@Nullable String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Check if word contained in string with ","
     *
     * @param word Word to search
     * @param str  String with words
     * @return Is word contained
     */
    public static boolean isWordInList(String word, String str) {
        String[] ln = str.split(",");
        for (String wordInList : ln) {
            if (wordInList.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static UUID getUUID(OfflinePlayer player) {
        return Bukkit.getOnlineMode() ?
                player.getUniqueId() :
                UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(StandardCharsets.UTF_8));
    }

    public static UUID getUUID(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        return player == null ?
                getUUID(Bukkit.getOfflinePlayer(playerName)) :
                getUUID(player);
    }

    /**
     * Escape java symbols (?)
     *
     * @param doco String to escape
     * @return Escaped string
     */
    public static String escapeJava(String doco) {
        if (doco == null)
            return "";

        StringBuilder b = new StringBuilder();
        for (char c : doco.toCharArray()) {
            switch (c) {
                case '\r' -> b.append("\\r");
                case '\n' -> b.append("\\n");
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                default -> b.append(c);
            }
        }
        return b.toString();
    }

    /**
     * Generate list with empty strings
     *
     * @param size Size of list
     * @return List with specified size
     */
    public static List<String> getEmptyList(int size) {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < size; i++) l.add("");
        return l;
    }

    /**
     * Get list of names of all online players
     *
     * @return List of names
     */
    public static List<String> getPlayersList() {
        List<String> players = new ArrayList<>(Bukkit.getOnlinePlayers().size());
        Bukkit.getOnlinePlayers().forEach(p -> players.add(p.getName()));
        return players;
    }

    /**
     * Check permissions
     *
     * @param user User to check
     * @param perm Permission to check
     * @return Is permission is null or user has permissions
     */
    public static boolean checkPermission(Permissible user, String perm) {
        return perm == null || user.hasPermission(perm);
    }

    /**
     * Search for notnull element in array
     *
     * @param def Default value if element wasn't found
     * @param obj Array of objects to search
     * @param <T> Type of object
     * @return Searched object or default if not found
     */
    @SafeVarargs
    public static <T> T searchNotNull(T def, T... obj) {
        for (T searched : obj)
            if (searched != null) return searched;
        return def;

    }

    /**
     * Get any enum by its name
     * @param <T> Enum type
     * @param clazz Enum class
     * @param name Name of enum
     * @return Corresponding enum, or null if not found
     */
    public static <T extends Enum<T>> T getEnum(Class<T> clazz, String name) {
        return getEnum(clazz, name, null);
    }

    /**
     * Get any enum by its name or default value if not found
     * @param <T> Enum type
     * @param clazz Enum class
     * @param name Name of enum
     * @param def Default value
     * @return Corresponding enum, or {@param def} if not found
     */
    public static <T extends Enum<T>> T getEnum(Class<T> clazz, String name, T def) {
        if (clazz != null && !Utils.isStringEmpty(name)) {
            try {
                return Enum.valueOf(clazz, name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return def;
    }

    public static @NotNull String[] getAliases(@NotNull Class<?> clazz) {
        if (clazz.isAnnotationPresent(Aliases.class)) {
            return clazz.getAnnotation(Aliases.class).value();
        }
        return EMPTY_ARRAY;
    }

    public static @NotNull String @NotNull [] getAliases(@NotNull Object obj) {
        return obj instanceof Aliased aliased
                ? aliased.getAliases()
                : getAliases(obj.getClass());
    }

    public static @NotNull String cutBuilder(@NotNull StringBuilder builder, int offset) {
        return offset < builder.length()
                ? builder.substring(0, builder.length() - offset)
                : "";
    }

    public static @Nullable Color getColor(@NotNull String value) {
        if (value.startsWith("#")) {
            Matcher matcher = HEX_COLOR.matcher(value);
            if (matcher.matches()) {
                return Color.fromRGB(Integer.parseInt(matcher.group(1), 16));
            }
        } else {
            Matcher matcher = BYTE_COLOR.matcher(value);
            if (matcher.matches()) {
                return Color.fromRGB(
                        Math.min(getInteger(matcher.group(1), 0), 255),
                        Math.min(getInteger(matcher.group(2), 0), 255),
                        Math.min(getInteger(matcher.group(3), 0), 255)
                );
            } else if (!value.isEmpty()) {
                TextColor color = NamedTextColor.NAMES.value(value.toUpperCase(Locale.ROOT));
                if (color != null) {
                    return Color.fromRGB(color.value());
                }
            }
        }
        return null;
    }
}
