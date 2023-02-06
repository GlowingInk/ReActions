package me.fromgate.reactions.util.collections;

import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Map;

public class Maps {
    public static <T> LinkedCaseInsensitiveMap<T> caseInsensitive(int size) {
        return new LinkedCaseInsensitiveMap<>(size);
    }

    public static <T> LinkedCaseInsensitiveMap<T> caseInsensitive() {
        return new LinkedCaseInsensitiveMap<>();
    }

    public static <T> LinkedCaseInsensitiveMap<T> caseInsensitive(Map<String, T> origin) {
        return new LinkedCaseInsensitiveMap<>(origin);
    }
}
