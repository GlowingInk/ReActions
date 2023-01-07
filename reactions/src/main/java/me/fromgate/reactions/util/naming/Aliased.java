package me.fromgate.reactions.util.naming;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface Aliased extends Named {
    @NotNull Collection <@NotNull String> aliases();

    static @NotNull Collection <@NotNull String> getAliases(@NotNull Class<?> clazz) {
        if (clazz.isAnnotationPresent(Names.class)) {
            return Arrays.asList(clazz.getAnnotation(Names.class).value());
        }
        return List.of();
    }

    static @NotNull Collection <@NotNull String> getAliases(@NotNull Object obj) {
        return obj instanceof Aliased aliased
                ? aliased.aliases()
                : getAliases(obj.getClass());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Names {
        String[] value();
    }
}
