package fun.reactions.util.parameter;

import fun.reactions.util.NumberUtils;
import fun.reactions.util.TimeUtils;
import fun.reactions.util.Utils;
import fun.reactions.util.collections.CaseInsensitiveMap;
import fun.reactions.util.enums.TriBoolean;
import fun.reactions.util.function.SafeFunction;
import fun.reactions.util.function.SafeSupplier;
import fun.reactions.util.item.VirtualItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

public class Parameters implements Parameterizable {
    public static final String ORIGIN = " :";
    public static final Parameters EMPTY = new Parameters("", "", new CaseInsensitiveMap(1));
    private static final Pattern UNESCAPED = Pattern.compile("(?<!\\\\)[{}]");

    private final String origin;
    private final Map<String, String> params;

    private String formatted;
    private Set<String> strictKeys;
    private Integer hash;

    protected Parameters(@NotNull String origin, @NotNull Map<String, String> params) {
        this.origin = origin;
        params.put(ORIGIN, origin);
        this.params = Collections.unmodifiableMap(params);
    }

    protected Parameters(@NotNull String origin, @NotNull String formatted, @NotNull Map<String, String> params) {
        this(origin, params);
        this.formatted = formatted;
    }

    public static @NotNull Parameters fromConfiguration(@NotNull ConfigurationSection cfg) {
        return fromConfiguration(cfg, Set.of());
    }

    public static @NotNull Parameters fromConfiguration(@NotNull ConfigurationSection cfg, @NotNull Set<String> ignoredKeys) {
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
                //noinspection ConstantConditions
                params.put(key, fromConfiguration(cfg.getConfigurationSection(key), ignoredKeys).toString());
            } else if (cfg.isItemStack(key)) {
                params.put(key, VirtualItem.asString(cfg.getItemStack(key)));
            } else {
                //noinspection ConstantConditions
                params.put(key, cfg.get(key).toString());
            }
        }
        return fromMap(params);
    }

    public static @NotNull Parameters fromMap(@NotNull Map<String, String> map) {
        if (map.isEmpty()) return EMPTY;
        Map<String, String> params = new CaseInsensitiveMap(map);
        String str = formatMap(map);
        return new Parameters(str, str, params);
    }

    public static @NotNull Parameters fromString(@NotNull String str) {
        return fromString(str, null);
    }

    public static @NotNull Parameters fromString(final @NotNull String str, @Nullable String defKey) {
        if (str.isEmpty()) return EMPTY;
        boolean hasDefKey = !Utils.isStringEmpty(defKey);
        Map<String, String> params = new CaseInsensitiveMap();
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
                    if (n == '{' || n == '}' || (n == '\\' && next + 1 < str.length() && str.charAt(next + 1) == '}')) {
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
                        } else {
                            --brCount;
                        }
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
                ? new Parameters(str, new CaseInsensitiveMap(1))
                : new Parameters(str, params);
    }

    private enum IterationState {
        SPACE, TEXT, COLON, PARAM, BR_PARAM
    }

    public static @NotNull Parameters singleton(@NotNull String key, @NotNull String value) {
        Map<String, String> params = new CaseInsensitiveMap(2);
        params.put(key, value);
        String escaped = escapeParameters(value);
        String origin;
        if (requiresBrackets(escaped, value)) {
            origin = key + ":{" + escaped + "}";
        } else {
            origin = key + ":" + escaped;
        }
        return new Parameters(origin, origin, params);
    }

    public static @NotNull String formatMap(@NotNull Map<String, String> map) {
        StringBuilder bld = new StringBuilder();
        map.forEach((key, value) -> {
            if (key.equals(ORIGIN)) return;
            bld.append(key).append(':');
            String escaped = escapeParameters(value);
            if (requiresBrackets(escaped, value)) {
                bld.append('{').append(escaped).append('}');
            } else {
                bld.append(value);
            }
            bld.append(' ');
        });
        return Utils.cutLast(bld, 1);
    }

    public static boolean requiresBrackets(@NotNull String escaped, @NotNull String value) {
        return value.length() >= 20 || escaped.length() != value.length() || value.isEmpty() ||
                value.indexOf(' ') != -1 || value.indexOf(':') != -1 || value.charAt(0) == '{';
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

    public <R> @NotNull R getSafe(@NotNull String key, @NotNull Function<String, R> converter, @NotNull SafeSupplier<R> def) {
        R value = get(key, converter);
        return value == null ? def.get() : value;
    }

    public <R> @NotNull R getSafe(@NotNull String key, @NotNull SafeFunction<String, R> converter) {
        return converter.apply(getString(key));
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

    public <R extends Enum<R>> @NotNull R getEnumSafe(@NotNull String key, @NotNull Class<R> clazz, @NotNull SafeSupplier<R> def) {
        return getSafe(key, (value) -> Utils.getEnum(clazz, value), def);
    }

    public @NotNull String getString(@NotNull String key) {
        return getString(key, "");
    }

    @Contract("_, !null -> !null")
    public @Nullable String getString(@NotNull String key, @Nullable String def) {
        return params.getOrDefault(key, def);
    }

    public @NotNull String getStringSafe(@NotNull String key, @NotNull SafeSupplier<String> def) {
        String value = params.get(key);
        return value == null ? def.get() : value;
    }

    public @NotNull Parameters getParameters(@NotNull String key) {
        return fromString(getString(key));
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
        return getTriBoolean(key).asBoolean(def);
    }

    public boolean getBoolean(@NotNull String key, @NotNull BooleanSupplier def) {
        return getTriBoolean(key).asBoolean(def);
    }

    public @NotNull TriBoolean getTriBoolean(@NotNull String key) {
        return getSafe(key, TriBoolean::byString);
    }

    public long getTime(@NotNull String key) {
        return TimeUtils.parseTime(getString(key));
    }

    public long getTime(@NotNull String key, long def) {
        String value = params.get(key);
        return value == null ? def : TimeUtils.parseTime(value);
    }

    public @NotNull Duration getDuration(@NotNull String key) {
        return getDuration(key, Duration.ZERO);
    }

    @Contract("_, !null -> !null")
    public @Nullable Duration getDuration(@NotNull String key, @Nullable Duration def) {
        return get(key, (str) -> Duration.ofMillis(TimeUtils.parseTime(str)), def);
    }

    public @Unmodifiable @NotNull List<@NotNull String> keyedList(@NotNull String baseKey) {
        baseKey = baseKey.toLowerCase(Locale.ROOT);
        String numberedKey = baseKey + "1";
        if (contains(numberedKey)) {
            List<String> keys = new ArrayList<>();
            keys.add(numberedKey);
            int i = 1;
            while (contains(numberedKey = baseKey + (++i))) {
                keys.add(numberedKey);
            }
            return Collections.unmodifiableList(keys);
        } else if (contains(baseKey)) {
            return List.of(baseKey);
        } else {
            return List.of();
        }
    }

    public void keyedListIterate(@NotNull String baseKey, @NotNull BiConsumer<String, Parameters> action) {
        baseKey = baseKey.toLowerCase(Locale.ROOT);
        String numberedKey = baseKey + "1";
        if (contains(numberedKey)) {
            action.accept(numberedKey, this);
            int i = 1;
            while (contains(numberedKey = baseKey + (++i))) {
                action.accept(numberedKey, this);
            }
        } else if (contains(baseKey)) {
            action.accept(baseKey, this);
        }
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
        return findKeyUnsafe(keys) != null;
    }

    public boolean containsAny(@NotNull Iterable<@NotNull String> keys) {
        return findKeyUnsafe(keys) != null;
    }

    public @Nullable String findKeyUnsafe(@NotNull String @NotNull ... keys) {
        return findKeyUnsafe(Arrays.asList(keys));
    }

    public @Nullable String findKeyUnsafe(@NotNull Iterable<@NotNull String> keys) {
        return findKey(null, keys);
    }

    @Contract("!null, _ -> !null")
    public @Nullable String findKey(@Nullable String def, @NotNull String key) {
        return isEmpty() || !contains(key) ? def : key;
    }

    @Contract("!null, _, _ -> !null")
    public @Nullable String findKey(@Nullable String def, @NotNull String key1, @NotNull String key2) {
        if (isEmpty()) return def;
        if (contains(key1)) return key1;
        if (contains(key2)) return key2;
        return def;
    }

    @Contract("!null, _, _, _ -> !null")
    public @Nullable String findKey(@Nullable String def, @NotNull String key1, @NotNull String key2, @NotNull String key3) {
        if (isEmpty()) return def;
        if (contains(key1)) return key1;
        if (contains(key2)) return key2;
        if (contains(key3)) return key3;
        return def;
    }

    @Contract("!null, _ -> !null")
    public @Nullable String findKey(@Nullable String def, @NotNull String @NotNull ... keys) {
        return findKey(def, Arrays.asList(keys));
    }

    @Contract("!null, _ -> !null")
    public @Nullable String findKey(@Nullable String def, @NotNull Iterable<@NotNull String> keys) {
        if (isEmpty()) return def;
        for (String key : keys) {
            if (contains(key)) {
                return key;
            }
        }
        return def;
    }

    @Contract(pure = true)
    public @NotNull Parameters with(@NotNull String key, @NotNull String value) {
        if (isEmpty()) return singleton(key, value);
        Map<String, String> updated = new LinkedHashMap<>(this.params);
        updated.put(key, value);
        return fromMap(updated);
    }

    @Contract(pure = true)
    public @NotNull Parameters with(@NotNull Parameters add) {
        if (isEmpty()) return add;
        if (add.isEmpty()) return this;
        return with(add.originMap());
    }

    @Contract(pure = true)
    public @NotNull Parameters with(@NotNull Map<String, String> add) {
        Map<String, String> updated = new LinkedHashMap<>(this.params);
        updated.putAll(add);
        return fromMap(updated);
    }

    public @NotNull String originFormatted() {
        return formatted == null
                ? (formatted = formatMap(params))
                : formatted;
    }

    public @NotNull String origin() {
        return origin;
    }

    public @Unmodifiable @NotNull Set<String> keys() {
        if (this.strictKeys == null) {
            Set<String> keys = new HashSet<>(params.keySet());
            keys.remove(ORIGIN);
            this.strictKeys = Collections.unmodifiableSet(keys);
        }
        return this.strictKeys;
    }

    public @Unmodifiable @NotNull Set<String> keysFull() {
        return params.keySet();
    }

    public @Unmodifiable @NotNull Map<String, String> originMap() {
        return params;
    }

    @Override
    public @NotNull Parameters asParameters() {
        return this;
    }

    public void forEach(@NotNull BiConsumer<@NotNull String, @NotNull String> action) {
        params.forEach((key, value) -> {
            if (!ORIGIN.equals(key)) action.accept(key, value);
        });
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return params.size() - 1;
    }

    @Override
    public int hashCode() {
        if (this.hash == null) {
            int hash = 1; // To skip (un)boxing on calculation
            for (String key : keys()) {
                hash = 31 * hash + key.toLowerCase(Locale.ROOT).hashCode() + getString(key).hashCode();
            }
            this.hash = hash;
        }
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Parameters other) {
            if (other.size() != size()) return false;
            for (String key : keys()) {
                if (!Objects.equals(getString(key), other.getString(key, null))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean equalsFull(@Nullable Parameters params) {
        return params != null && (params == this || params.origin.equals(origin));
    }

    @Override
    public @NotNull String toString() {
        return originFormatted();
    }
}
