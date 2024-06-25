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

package fun.reactions.util;

import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameters;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
    private static final Pattern HEX_COLOR = Pattern.compile("#([a-fA-F\\d]{6})");
    private static final Pattern BYTE_COLOR = Pattern.compile("(\\d{1,3}),(\\d{1,3}),(\\d{1,3})");

    private Utils() {}

    /**
     * Play sound on location
     *
     * @param loc    Location where sound should be played
     * @param params Parameters of sound
     * @return Name of played sound
     */
    public static String soundPlay(@Nullable Location loc, @NotNull Parameters params) {
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
            volume = (float) NumberUtils.parseDouble(strvolume, Is.NON_NEGATIVE).orElse(1);
            pitch = (float) NumberUtils.parseDouble(strvolume, Is.NON_NEGATIVE).orElse(1);
        } else {
            String locationStr = params.getString("loc");
            soundLoc = locationStr.isEmpty() ? loc : LocationUtils.parseLocation(locationStr, null);
            sndstr = params.getString("type", "");
            pitch = (float) params.getDouble("pitch", 1.0f);
            volume = (float) params.getDouble("volume", 1.0f);
        }
        Sound sound = getEnum(sndstr, Sound.UI_BUTTON_CLICK); // TODO Custom sounds support
        if (soundLoc != null) soundLoc.getWorld().playSound(soundLoc, sound, volume, pitch);
        return sound.name();
    }

    /**
     * Play sound on location
     *
     * @param loc   Location where sound should be played
     * @param param Parameters of sound
     */
    public static void soundPlay(@NotNull Location loc, @NotNull String param) {
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
    public static boolean containsWord(@NotNull String word, @NotNull String str) {
        String[] ln = str.split(",");
        for (String wordInList : ln) {
            if (wordInList.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static @NotNull UUID getPlayerId(@NotNull String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        return player == null ?
                Optional.ofNullable(Bukkit.getOfflinePlayerIfCached(playerName)).orElse(Bukkit.getOfflinePlayer(playerName)).getUniqueId() :
                player.getUniqueId();
    }

    /**
     * Escape java symbols (?)
     * No one knows why it exists
     *
     * @param doco String to escape
     * @return Escaped string
     */
    public static @NotNull String escapeJava(@Nullable String doco) {
        if (doco == null) {
            return "";
        }

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
    public static @NotNull List<@NotNull String> getFilledEmptyList(int size) {
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add("");
        return list;
    }

    /**
     * Get list of names of all online players
     *
     * @return List of names
     */
    public static @NotNull List<@NotNull String> getPlayersList() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        List<String> playersNames = new ArrayList<>(players.size());
        players.forEach(p -> playersNames.add(p.getName()));
        return playersNames;
    }

    /**
     * Check permissions
     *
     * @param user User to check
     * @param perm Permission to check
     * @return Is permission not-null and user doesn't have it
     */
    public static boolean isRestricted(@NotNull Permissible user, @Nullable String perm) {
        return perm != null && !user.hasPermission(perm);
    }

    /**
     * Search for notnull element in array
     *
     * @param def Default value if element wasn't found
     * @param obj Array of objects to search
     * @param <T> Type of object
     * @return Searched object or default if not found
     */
    @Contract("!null, _ -> !null")
    @SafeVarargs
    public static <T> T searchNotNull(@Nullable T def, @Nullable T @NotNull ... obj) {
        for (T searched : obj) {
            if (searched != null) return searched;
        }
        return def;
    }

    /**
     * Get any enum by its name
     * @param <T> Enum type
     * @param clazz Enum class
     * @param name Name of enum
     * @return Corresponding enum, or null if not found
     */
    public static <T extends Enum<T>> @Nullable T getEnum(@NotNull Class<T> clazz, @NotNull String name) {
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
    @Contract("_, _, !null -> !null")
    public static <T extends Enum<T>> @Nullable T getEnum(@NotNull Class<T> clazz, @NotNull String name, @Nullable T def) {
        try {
            return Enum.valueOf(clazz, name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return def;
        }
    }

    /**
     * Get any enum by its name or default value if not found
     * @param <T> Enum type
     * @param name Name of enum
     * @param def Default value
     * @return Corresponding enum, or {@param def} if not found
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> @NotNull T getEnum(@NotNull String name, @NotNull T def) {
        try {
            return (T) Enum.valueOf(def.getClass(), name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return def;
        }
    }

    public static @NotNull String cutLast(@NotNull StringBuilder builder, int offset) {
        return offset < builder.length()
                ? builder.substring(0, builder.length() - offset)
                : "";
    }

    public static @NotNull String cutLast(@NotNull String str, int offset) {
        return offset < str.length()
                ? str.substring(0, str.length() - offset)
                : "";
    }

    public static @Nullable Color getColor(@Nullable String value) {
        if (Utils.isStringEmpty(value)) return null;
        if (value.startsWith("#")) {
            Matcher matcher = HEX_COLOR.matcher(value);
            if (matcher.matches()) {
                return Color.fromRGB(Integer.parseInt(matcher.group(1), 16));
            }
        } else {
            Matcher matcher = BYTE_COLOR.matcher(value);
            if (matcher.matches()) {
                return Color.fromRGB(
                        Math.min(NumberUtils.asInteger(matcher.group(1), 0), 255),
                        Math.min(NumberUtils.asInteger(matcher.group(2), 0), 255),
                        Math.min(NumberUtils.asInteger(matcher.group(3), 0), 255)
                );
            } else {
                TextColor color = NamedTextColor.NAMES.value(value.toLowerCase(Locale.ROOT));
                if (color != null) {
                    return Color.fromRGB(color.value());
                }
            }
        }
        return null;
    }

    public static @NotNull List<String> literalSplit(@NotNull String str, @NotNull String delim) {
        List<String> l = new ArrayList<>();
        int offset = 0;
        while (true) {
            int index = str.indexOf(delim, offset);
            if (index == -1) {
                l.add(str.substring(offset));
                return l;
            } else {
                l.add(str.substring(offset, index));
                offset = index + delim.length();
            }
        }
    }

    public static @Nullable UUID parseUniqueId(@Nullable String str) {
        if (isStringEmpty(str)) return null;
        try {
            return UUID.fromString(str);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static <T extends Keyed> @Nullable T searchRegistry(@NotNull String keyStr, @NotNull Registry<T> registry) {
        NamespacedKey key = NamespacedKey.fromString(keyStr.toLowerCase(Locale.ROOT));
        return key == null ? null : registry.get(key);
    }
}
