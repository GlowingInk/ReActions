package fun.reactions.util.naming;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface Aliased extends Named {
    @NotNull Collection<@NotNull String> getAliases();

    static @Unmodifiable @NotNull Collection<@NotNull String> getAliasesOf(@NotNull Class<?> clazz) {
        if (clazz.isAnnotationPresent(Names.class)) {
            return Arrays.asList(clazz.getAnnotation(Names.class).value());
        }
        return List.of();
    }

    static @Unmodifiable @NotNull Collection<@NotNull String> getAliasesOf(@NotNull Object obj) {
        return obj instanceof Aliased aliased
                ? Collections.unmodifiableCollection(aliased.getAliases())
                : getAliasesOf(obj.getClass());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Names {
        String[] value();
    }
}
