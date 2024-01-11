package fun.reactions.placeholders.resolvers;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Resolver<T extends Placeholder> permits KeyedResolver, DynamicResolver {
    boolean add(@NotNull T ph);

    @Nullable String resolve(@NotNull Environment env, @NotNull String text);

    @NotNull Collection<T> getPlaceholders();
}
