package me.fromgate.reactions.util.parameter;

import me.fromgate.reactions.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ParametersUtils {
    private static final Pattern UNESCAPED = Pattern.compile("(?<!\\\\)[{}]");

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
                case ' ' -> {
                    inside = false;
                    if (' ' == splitCh) {
                        int nextIndex = index + 1;
                        if (brCount == 0) {
                            splits.add(str.substring(lastSplit, index));
                            lastSplit = nextIndex;
                        }
                    }
                }
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

    public static @NotNull String formatMap(@NotNull Map<String, String> map) {
        StringBuilder bld = new StringBuilder();
        map.forEach((key, value) -> {
            if (key.equals(Parameters.ORIGIN)) return;
            bld.append(key).append(':');
            String escaped = escapeParameters(value);
            if (requiresBrackets(escaped, value)) {
                bld.append('{').append(escaped).append('}');
            } else {
                bld.append(value);
            }
            bld.append(' ');
        });
        return Utils.cutBuilder(bld, 1);
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
}
