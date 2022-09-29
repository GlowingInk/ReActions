package me.fromgate.reactions.placeholders.resolvers;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Resolver<T extends Placeholder> permits EqualResolver, PostprocessResolver, PrefixedResolver, SimpleResolver {
    boolean put(@NotNull T ph);

    @Nullable String parse(@NotNull RaContext context, @NotNull String text);

    @NotNull Collection<T> getPlaceholders();
}
