package fun.reactions.commands.plugin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RegistryArgument<T> implements CustomArgumentType.Converted<T, String> {
    private final Function<String, @Nullable T> resolver;
    private final Supplier<Collection<String>> suggestions;

    private RegistryArgument(
            @NotNull Function<String, @Nullable T> resolver,
            @NotNull Supplier<Collection<String>> suggestions
    ) {
        this.resolver = resolver;
        this.suggestions = suggestions;
    }

    public static <T> @NotNull RegistryArgument<T> registryArgument(
            @NotNull Function<String, @Nullable T> resolver,
            @NotNull Supplier<Collection<String>> suggestions
    ) {
        return new RegistryArgument<>(resolver, suggestions);
    }

    @Override
    public @NotNull T convert(@NotNull String value) throws CommandSyntaxException {
        T result = resolver.apply(value);
        if (result == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        }
        return result;
    }

    @Override
    public @NotNull StringArgumentType getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(
            @NotNull CommandContext<S> context,
            @NotNull SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining();
        suggestions.get().stream()
                .filter(s -> s.startsWith(remaining))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
