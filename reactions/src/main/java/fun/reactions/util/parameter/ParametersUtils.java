package fun.reactions.util.parameter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ParametersUtils {
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
}
