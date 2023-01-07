package me.fromgate.reactions.util.alias;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface Aliased {
    @NotNull Collection <@NotNull String> aliases();

    static @NotNull Collection <@NotNull String> getAliases(@NotNull Class<?> clazz) {
        if (clazz.isAnnotationPresent(Aliases.class)) {
            return Arrays.asList(clazz.getAnnotation(Aliases.class).value());
        }
        return List.of();
    }

    static @NotNull Collection <@NotNull String> getAliases(@NotNull Object obj) {
        return obj instanceof Aliased aliased
                ? aliased.aliases()
                : getAliases(obj.getClass());
    }
}
