package fun.reactions.util.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Maps {
    public static <T> @NotNull Map<String, T> caseInsensitive(int size) {
        return new CaseInsensitiveMap<>(size);
    }

    public static <T> @NotNull Map<String, T> caseInsensitive() {
        return new CaseInsensitiveMap<>();
    }

    public static <T> @NotNull Map<String, T> caseInsensitive(Map<String, T> origin) {
        return new CaseInsensitiveMap<>(origin);
    }
}
