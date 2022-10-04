package me.fromgate.reactions.util.alias;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Aliased {
    @NotNull String @NotNull [] getAliases();
}
