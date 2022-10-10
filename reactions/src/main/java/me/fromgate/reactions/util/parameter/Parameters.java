package me.fromgate.reactions.util.parameter;

import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.collections.CaseInsensitiveMap;
import me.fromgate.reactions.util.suppliers.NotNullSupplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

public class Parameters implements Iterable<String> {
    public static final String ORIGIN_KEY = "origin-string";
    public static final Parameters EMPTY = new Parameters("", Collections.emptyMap());

    private final String origin;
    private final Map<String, String> params;

    protected Parameters(@NotNull String origin, @NotNull Map<String, String> params) {
        this.origin = origin;
        this.params = params;
    }

    public static @NotNull List<String> splitSafely(@NotNull String str, char splitCh) { // TODO: Edge case "test:value,value"
        if (str.indexOf(splitCh) == -1) return Collections.singletonList(str);
        List<String> splits = new ArrayList<>();
        int lastSplit = 0;
        int brCount = 0;
        for (int index = 0; index < str.length(); ++index) {
            char ch = str.charAt(index);
            if (ch == splitCh) {
                if (brCount == 0) {
                    int nextIndex = index + 1;
                    splits.add(str.substring(lastSplit, nextIndex));
                    lastSplit = nextIndex;
                }
            } else if (ch == '{') {
                ++brCount;
            } else if (ch == '}') {
                --brCount;
            }
        }
        return splits;
    }

    public static @NotNull Parameters fromString(@NotNull String str) {
        return fromString(str, null);
    }

    public static @NotNull Parameters fromString(@NotNull String str, @Nullable String defKey) { // TODO: Escaping
        boolean hasDefKey = !Utils.isStringEmpty(defKey);
        Map<String, String> params = new CaseInsensitiveMap<>(true);
        IterationState state = IterationState.SPACE;
        String param = "";
        StringBuilder bld = null; // TODO: Implement building style from placeholders? JMH performance testing required
        int brCount = 0;
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
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
                            String value = bld.toString();
                            params.put(defKey, value);
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
                        String value = bld.toString();
                        params.put(param, value);
                        continue;
                    }
                    bld.append(c);
                }
                case BR_PARAM -> {
                    if (c == '}') {
                        if (brCount == 0) {
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

        params.put(Parameters.ORIGIN_KEY, str);
        return new Parameters(str, params);
    }

    private enum IterationState {
        SPACE, TEXT, COLON, PARAM, BR_PARAM
    }

    public static @NotNull Parameters noParse(@NotNull String str) {
        Map<String, String> params = new CaseInsensitiveMap<>(1);
        params.put(Parameters.ORIGIN_KEY, str);
        return new Parameters(str, params);
    }

    public static @NotNull Parameters noParse(@NotNull String str, @NotNull String defKey) {
        Map<String, String> params = new CaseInsensitiveMap<>();
        params.put(defKey, str);
        params.put(Parameters.ORIGIN_KEY, str);
        return new Parameters(str, params);
    }

    public static @NotNull Parameters fromMap(@NotNull Map<String, String> map) {
        StringBuilder bld = new StringBuilder();
        Map<String, String> params = new CaseInsensitiveMap<>(map);
        map.forEach((k, v) -> {
            bld.append(k).append(':');
            if (v.indexOf(' ') != -1) {
                bld.append('{').append(v).append('}');
            } else {
                bld.append(v);
            }
            bld.append(' ');
        });
        return new Parameters(Utils.cutBuilder(bld, 1), params);
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

    public <R> @NotNull R getSafe(@NotNull String key, @NotNull Function<String, R> converter, @NotNull NotNullSupplier<R> def) {
        R value = get(key, converter);
        return value == null ? def.get() : value;
    }

    public <R extends Enum<R>> @Nullable R getEnum(@NotNull String key, @NotNull Class<R> clazz) {
        return Utils.getEnum(clazz, key);
    }

    @Contract("_, _, !null -> !null")
    public <R extends Enum<R>> @Nullable R getEnum(@NotNull String key, @NotNull Class<R> clazz, @Nullable R def) {
        return Utils.getEnum(clazz, key, def);
    }

    public @NotNull String getString(@NotNull String key) {
        return getString(key, "");
    }

    @Contract("_, !null -> !null")
    public @Nullable String getString(@NotNull String key, @Nullable String def) {
        return params.getOrDefault(key, def);
    }

    public @NotNull String getStringSafe(@NotNull String key, @NotNull NotNullSupplier<String> def) {
        String value = params.get(key);
        return value == null ? def.get() : value;
    }

    public double getDouble(@NotNull String key) {
        return getDouble(key, 0);
    }

    public double getDouble(@NotNull String key, double def) {
        return NumberUtils.getDouble(params.get(key), def);
    }

    public double getDouble(@NotNull String key, @NotNull DoubleSupplier def) {
        return NumberUtils.getDouble(params.get(key), def);
    }

    public int getInteger(@NotNull String key) {
        return getInteger(key, 0);
    }

    public int getInteger(@NotNull String key, int def) {
        return NumberUtils.getInteger(params.get(key), def);
    }

    public int getInteger(@NotNull String key, @NotNull IntSupplier def) {
        return NumberUtils.getInteger(params.get(key), def);
    }

    public boolean getBoolean(@NotNull String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(@NotNull String key, boolean def) {
        if (params.containsKey(key)) {
            return switch (params.get(key).toLowerCase(Locale.ROOT)) {
                case "true" -> true;
                case "false" -> false;
                default -> def;
            };
        } else return def;
    }

    public boolean getBoolean(@NotNull String key, @NotNull BooleanSupplier def) {
        if (params.containsKey(key)) {
            return switch (params.get(key).toLowerCase(Locale.ROOT)) {
                case "true" -> true;
                case "false" -> false;
                default -> def.getAsBoolean();
            };
        } else return def.getAsBoolean();
    }

    public boolean contains(@NotNull String key) {
        return params.containsKey(key);
    }

    public boolean containsEvery(@NotNull Iterable<String> keys) {
        for (String key : keys) {
            if (!params.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsEvery(@NotNull String @NotNull ... keys) {
        return containsEvery(List.of(keys));
    }

    public boolean containsAny(@NotNull Iterable<String> keys) {
        for (String key : keys) {
            if (params.containsKey(key)) return true;
        }
        return false;
    }

    public boolean containsAny(@NotNull String @NotNull ... keys) {
        return containsAny(List.of(keys));
    }

    @Deprecated
    public boolean matchesAny(@NotNull Pattern @NotNull ... patterns) { // TODO: Quite useless
        for (Pattern pattern : patterns) {
            for (String param : params.keySet()) {
                if (pattern.matcher(param).matches()) return true;
            }
        }
        return false;
    }

    public @UnmodifiableView @NotNull Set<String> keySet() {
        return Collections.unmodifiableSet(this.params.keySet());
    }

    public @UnmodifiableView @NotNull Map<String, String> getMap() {
        return Collections.unmodifiableMap(this.params);
    }

    public boolean isEmpty() {
        return this.params.isEmpty();
    }

    @Deprecated
    public @Nullable String put(@NotNull String key, @NotNull String value) {
        return params.put(key, value);
    }

    public int size() {
        return this.params.size();
    }

    @Override
    public @NotNull String toString() {
        return this.origin;
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return getMap().keySet().iterator();
    }

    public void forEach(@NotNull BiConsumer<String, String> consumer) {
        params.forEach(consumer);
    }
}
