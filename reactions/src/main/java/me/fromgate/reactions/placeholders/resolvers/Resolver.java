package me.fromgate.reactions.placeholders.resolvers;

import me.fromgate.reactions.logic.environment.Environment;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Resolver<T extends Placeholder> permits PreprocessResolver, KeyedResolver, DynamicResolver {
    boolean put(@NotNull T ph);

    @Nullable String parse(@NotNull Environment env, @NotNull String text);

    @NotNull Collection<T> getPlaceholders();
}
