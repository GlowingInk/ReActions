package fun.reactions.util.collections;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Map;

public class Maps {
    public static <T> @NotNull LinkedCaseInsensitiveMap<T> caseInsensitive(int size) {
        return new LinkedCaseInsensitiveMap<>(size);
    }

    public static <T> @NotNull LinkedCaseInsensitiveMap<T> caseInsensitive() {
        return new LinkedCaseInsensitiveMap<>();
    }

    public static <T> @NotNull LinkedCaseInsensitiveMap<T> caseInsensitive(Map<String, T> origin) {
        return new LinkedCaseInsensitiveMap<>(origin);
    }
}
