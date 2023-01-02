package me.fromgate.reactions.util.parameter;

import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.collections.Maps;
import me.fromgate.reactions.util.function.SafeSupplier;
import me.fromgate.reactions.util.item.VirtualItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Parameters implements Iterable<String>, Parameterizable {
    public static final String ORIGIN = "origin:string";
    public static final Parameters EMPTY = new Parameters("", "", Maps.caseInsensitive(1));

    private static final Pattern UNESCAPED = Pattern.compile("(?<!\\\\)[{}]");

    private final String origin;
    private final Map<String, String> params;

    private String formatted;
    private Set<String> keys;

    protected Parameters(@NotNull String origin, @NotNull Map<String, String> params) {
        this.origin = origin;
        params.put(Parameters.ORIGIN, origin);
        this.params = Collections.unmodifiableMap(params);
    }

    protected Parameters(@NotNull String origin, @NotNull String formatted, @NotNull Map<String, String> params) {
        this(origin, params);
        this.formatted = formatted;
    }

    public static @NotNull List<@NotNull String> splitSafely(@NotNull String str, char splitCh) {
        if (str.indexOf(splitCh) == -1) return List.of(str);
        List<String> splits = new ArrayList<>();
        int lastSplit = 0;
        int brCount = 0;
        boolean inside = false;
        for (int index = 0; index < str.length(); ++index) {
            char ch = str.charAt(index);
            switch (ch) {
                case '\\' -> {
                    int next = index + 1;
                    if (str.length() != next) {
                        char n = str.charAt(next);
                        if (n == '{' || n == '}') {
                            ++index;
                        }
                    }
                }
                case '{' -> ++brCount;
                case '}' -> brCount = Math.max(0, brCount - 1);
                case ':' -> {
                    int next = index + 1;
                    if (str.length() != next) {
                        char n = str.charAt(next);
                        if (n != '{') {
                            inside = true;
                        }
                    }
                }
                case ' ' -> inside = false;
                default -> {
                    if (ch == splitCh) {
                        int nextIndex = index + 1;
                        if (brCount == 0 && !inside || (inside && (str.length() == nextIndex || str.charAt(nextIndex) == ' '))) {
                            splits.add(str.substring(lastSplit, index));
                            lastSplit = nextIndex;
                        }
                    }
                }
            }
        }
        if (lastSplit != 0) {
            splits.add(str.substring(lastSplit));
        } else if (splits.isEmpty()) {
            splits.add(str);
        }
        return splits;
    }

    public static @NotNull Parameters fromConfiguration(@NotNull ConfigurationSection cfg) {
        return fromConfiguration(cfg, Set.of());
    }

    @SuppressWarnings("ConstantConditions")
    public static @NotNull Parameters fromConfiguration(@NotNull ConfigurationSection cfg, @NotNull Collection<String> ignoredKeys) {
        Map<String, String> params = new LinkedHashMap<>();
        for (String key : cfg.getKeys(false)) {
            if (ignoredKeys.contains(key)) continue;
            if (cfg.isString(key)) {
                params.put(key, cfg.getString(key, ""));
            } else if (cfg.isList(key)) {
                int i = 1;
                for (Object obj : cfg.getList(key, List.of())) {
                    if (obj instanceof ItemStack item) {
                        params.put(key + i, VirtualItem.asString(item));
                    } else if (obj instanceof ConfigurationSection listCfg) {
                        params.put(key + i, fromConfiguration(listCfg, ignoredKeys).toString());
                    } else {
                        params.put(key + i, obj.toString());
                    }
                    ++i;
                }
            } else if (cfg.isConfigurationSection(key)) {
                params.put(key, fromConfiguration(cfg.getConfigurationSection(key), ignoredKeys).toString());
            } else if (cfg.isItemStack(key)) {
                params.put(key, VirtualItem.asString(cfg.getItemStack(key)));
            } else {
                params.put(key, cfg.get(key).toString());
            }
        }
        return fromMap(params);
    }

    public static @NotNull Parameters fromMap(@NotNull Map<String, String> map) {
        if (map.isEmpty()) return Parameters.EMPTY;
        Map<String, String> params = Maps.caseInsensitive(map);
        String str = formatMap(map);
        return new Parameters(str, str, params);
    }

    public static @NotNull Parameters fromString(@NotNull String str) {
        return fromString(str, null);
    }

    public static @NotNull Parameters fromString(@NotNull String str, @Nullable String defKey) {
        if (str.isEmpty()) return Parameters.EMPTY;
        boolean hasDefKey = !Utils.isStringEmpty(defKey);
        Map<String, String> params = Maps.caseInsensitive();
        IterationState state = IterationState.SPACE;
        String param = "";
        StringBuilder bld = null;
        int brCount = 0;
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (c == '\\') {
                int next = i + 1;
                if (next < str.length()) {
                    char n = str.charAt(next);
                    if (n == '{' || n == '}' || (n == '\\' && i + 2 < str.length() && str.charAt(i + 2) == '}')) {
                        if (state == IterationState.SPACE) {
                            bld = new StringBuilder();
                            state = IterationState.TEXT;
                        }
                        bld.append(n);
                        i = next;
                        continue;
                    }
                }
            }
            switch (state) {
                case SPACE -> {
                    if (c == ' ') {
                        continue;
                    }
                    bld = new StringBuilder().append(c);
                    state = IterationState.TEXT;
                }
                case TEXT -> {
                    if (c == ' ') {
                        if (hasDefKey) {
                            params.put(defKey, bld.toString());
                        }
                        state = IterationState.SPACE;
                        continue;
                    }
                    if (c == ':') {
                        state = IterationState.COLON;
                        param = bld.toString();
                        bld = new StringBuilder();
                        continue;
                    }
                    bld.append(c);
                }
                case COLON -> {
                    if (c == ' ') {
                        state = IterationState.SPACE;
                        continue;
                    }
                    if (c == '{') {
                        state = IterationState.BR_PARAM;
                        continue;
                    }
                    bld.append(c);
                    state = IterationState.PARAM;
                }
                case PARAM -> {
                    if (c == ' ') {
                        state = IterationState.SPACE;
                        params.put(param, bld.toString());
                        continue;
                    }
                    bld.append(c);
                }
                case BR_PARAM -> {
                    if (c == '}') {
                        if (brCount < 1) {
                            state = IterationState.SPACE;
                            String value = bld.toString();
                            params.put(param, value);
                            continue;
                        } else --brCount;
                    } else if (c == '{') {
                        ++brCount;
                    }
                    bld.append(c);
                }
            }
        }

        if (state == IterationState.PARAM) {
            params.put(param, bld.toString());
        } else if (hasDefKey && state == IterationState.TEXT) {
            params.put(defKey, bld.toString());
        }

        return params.isEmpty()
                ? Parameters.EMPTY
                : new Parameters(str, params);
    }

    private enum IterationState {
        SPACE, TEXT, COLON, PARAM, BR_PARAM
    }

    public static @NotNull Parameters noParse(@NotNull String str) {
        Map<String, String> params = Maps.caseInsensitive(1);
        return new Parameters(str, params);
    }

    public static @NotNull Parameters noParse(@NotNull String str, @NotNull String defKey) {
        Map<String, String> params = Maps.caseInsensitive(2);
        params.put(defKey, str);
        return new Parameters(str, params);
    }

    public static @NotNull String formatMap(@NotNull Map<String, String> map) {
        StringBuilder bld = new StringBuilder();
        map.forEach((key, value) -> {
            if (key.equals(Parameters.ORIGIN)) return;
            bld.append(key).append(':');
            String escaped = escapeParameters(value);
            if (value.length() >= 20 || escaped.length() != value.length() || value.isEmpty() ||
                    value.indexOf(' ') != -1 || value.indexOf(':') != -1 || value.charAt(0) == '{') {
                bld.append('{').append(escaped).append('}');
            } else {
                bld.append(value);
            }
            bld.append(' ');
        });
        return Utils.cutBuilder(bld, 1);
    }

    public static @NotNull String escapeParameters(@NotNull String str) {
        if (str.isEmpty()) return str;
        int brackets = 0;
        boolean escaped = false;
        for (int i = 0; i < str.length(); i++) {
            if (escaped) {
                escaped = false;
                continue;
            }
            char ch = str.charAt(i);
            if (ch == '\\') {
                escaped = true;
            } else if (ch == '{') {
                ++brackets;
            } else if (ch == '}' && --brackets < 0) {
                break;
            }
        }
        if (str.charAt(str.length() - 1) == '\\' && (str.length() == 1 || str.charAt(str.length() - 2) != '\\')) {
            str += '\\';
        }
        return brackets != 0
                ? UNESCAPED.matcher(str).replaceAll("\\\\$0")
                : str;
    }

    public <R> @Nullable R get(@NotNull String key, @NotNull Function<String, R> converter) {
        String value = params.get(key);
        return value == null
                ? null
                : converter.apply(value);
    }

    @Contract("_, _, !null -> !null")
    public <R> @Nullable R get(@NotNull String key, @NotNull Function<String, R> converter, @Nullable R def) {
        R value = get(key, converter);
        return value == null ? def : value;
    }

    public <R> @Nullable R getSupplied(@NotNull String key, @NotNull Function<String, R> converter, @NotNull Supplier<R> def) {
        R value = get(key, converter);
        return value == null ? def.get() : value;
    }

    public <R> @NotNull R getSafe(@NotNull String key, @NotNull Function<String, R> converter, @NotNull SafeSupplier<R> def) {
        R value = get(key, converter);
        return value == null ? def.get() : value;
    }

    public <R extends Enum<R>> @NotNull R getEnum(@NotNull String key, @NotNull R def) {
        String value = params.get(key);
        return value == null
                ? def
                : Utils.getEnum(value, def);
    }

    public <R extends Enum<R>> @Nullable R getEnum(@NotNull String key, @NotNull Class<R> clazz) {
        return get(key, (value) -> Utils.getEnum(clazz, value));
    }

    @Contract("_, _, !null -> !null")
    public <R extends Enum<R>> @Nullable R getEnum(@NotNull String key, @NotNull Class<R> clazz, @Nullable R def) {
        return get(key, (value) -> Utils.getEnum(clazz, value, def));
    }

    public <R extends Enum<R>> @Nullable R getEnumSupplied(@NotNull String key, @NotNull Class<R> clazz, @NotNull Supplier<R> def) {
        return getSupplied(key, (value) -> Utils.getEnum(clazz, value), def);
    }

    public <R extends Enum<R>> @NotNull R getEnumSafe(@NotNull String key, @NotNull Class<R> clazz, @NotNull SafeSupplier<R> def) {
        return getSafe(key, (value) -> Utils.getEnum(clazz, value), def);
    }

    @Contract(pure = true)
    public @NotNull Parameters getParams(@NotNull String key) {
        return Parameters.fromString(getString(key));
    }

    public @NotNull String getString(@NotNull String key) {
        return getString(key, "");
    }

    @Contract("_, !null -> !null")
    public @Nullable String getString(@NotNull String key, @Nullable String def) {
        return params.getOrDefault(key, def);
    }

    public @Nullable String getStringSupplied(@NotNull String key, @NotNull Supplier<String> def) {
        String value = params.get(key);
        return value == null ? def.get() : value;
    }

    public @NotNull String getStringSafe(@NotNull String key, @NotNull SafeSupplier<String> def) {
        String value = params.get(key);
        return value == null ? def.get() : value;
    }

    public double getDouble(@NotNull String key) {
        return getDouble(key, 0);
    }

    public double getDouble(@NotNull String key, double def) {
        return NumberUtils.asDouble(params.get(key), def);
    }

    public double getDouble(@NotNull String key, @NotNull DoubleSupplier def) {
        return NumberUtils.asDouble(params.get(key), def);
    }

    public int getInteger(@NotNull String key) {
        return getInteger(key, 0);
    }

    public int getInteger(@NotNull String key, int def) {
        return NumberUtils.asInteger(params.get(key), def);
    }

    public int getInteger(@NotNull String key, @NotNull IntSupplier def) {
        return NumberUtils.asInteger(params.get(key), def);
    }

    public boolean getBoolean(@NotNull String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(@NotNull String key, boolean def) {
        String value = params.get(key);
        if (value != null) {
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "true" -> true;
                case "false" -> false;
                default -> def;
            };
        } else return def;
    }

    public boolean getBoolean(@NotNull String key, @NotNull BooleanSupplier def) {
        String value = params.get(key);
        if (value != null) {
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "true" -> true;
                case "false" -> false;
                default -> def.getAsBoolean();
            };
        } else return def.getAsBoolean();
    }

    public @Unmodifiable @NotNull List<@NotNull String> getKeyList(@NotNull String baseKey) {
        if (contains(baseKey + "1")) {
            List<String> keys = new ArrayList<>();
            keys.add(baseKey + "1");
            int i = 1;
            String key;
            while (contains(key = baseKey + (++i))) {
                keys.add(key);
            }
            return Collections.unmodifiableList(keys);
        } else if (contains(baseKey)) {
            return List.of(baseKey);
        } else {
            return List.of();
        }
    }

    public boolean contains(@NotNull String key, @NotNull Predicate<String> check) {
        return check.test(getString(key));
    }

    public boolean contains(@NotNull String key) {
        return params.containsKey(key);
    }

    public boolean containsEvery(@NotNull String @NotNull ... keys) {
        return containsEvery(Arrays.asList(keys));
    }

    public boolean containsEvery(@NotNull Collection<@NotNull String> keys) {
        if (keys.size() > size()) return false;
        for (String key : keys) {
            if (!contains(key)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAny(@NotNull String @NotNull ... keys) {
        return containsAny(Arrays.asList(keys));
    }

    public boolean containsAny(@NotNull Collection<@NotNull String> keys) {
        if (isEmpty()) return false;
        for (String key : keys) {
            if (contains(key)) {
                return true;
            }
        }
        return false;
    }

    @Contract(pure = true)
    public @NotNull Parameters with(@NotNull String key, @NotNull String value) {
        Map<String, String> updated = new LinkedHashMap<>(this.params);
        updated.put(key, value);
        return Parameters.fromMap(updated);
    }

    @Contract(pure = true)
    public @NotNull Parameters with(@NotNull Parameters params) {
        return with(params.originMap());
    }

    @Contract(pure = true)
    public @NotNull Parameters with(@NotNull Map<String, String> params) {
        Map<String, String> updated = new LinkedHashMap<>(this.params);
        updated.putAll(params);
        return Parameters.fromMap(updated);
    }

    public @NotNull String originFormatted() {
        return formatted == null
                ? (formatted = formatMap(params))
                : formatted;
    }

    public @NotNull String origin() {
        return origin;
    }

    @Override
    public @Unmodifiable @NotNull Iterator<String> iterator() {
        return keySetSafe().iterator();
    }

    public @Unmodifiable @NotNull Set<String> keySetSafe() {
        if (this.keys == null) {
            Set<String> keys = new HashSet<>(params.keySet());
            keys.remove(Parameters.ORIGIN);
            this.keys = Collections.unmodifiableSet(keys);
        }
        return this.keys;
    }

    public @Unmodifiable @NotNull Set<String> keySet() {
        return params.keySet();
    }

    public @Unmodifiable @NotNull Map<String, String> originMap() {
        return params;
    }

    @Override
    public @NotNull Parameters asParameters() {
        return this;
    }

    public boolean isEmpty() {
        return params.size() == 1;
    }

    public int size() {
        return params.size() - 1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Parameters other && other.originMap().equals(originMap());
    }

    @Override
    public int hashCode() {
        return params.hashCode();
    }

    @Override
    public @NotNull String toString() {
        return originFormatted();
    }
}
