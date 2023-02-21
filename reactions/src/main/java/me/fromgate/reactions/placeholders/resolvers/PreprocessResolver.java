package me.fromgate.reactions.placeholders.resolvers;

import me.fromgate.reactions.logic.environment.Environment;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class PreprocessResolver implements Resolver<Placeholder.Preprocess> {
    private final List<Placeholder.Preprocess> placeholders = new ArrayList<>();

    @Override
    public boolean put(@NotNull Placeholder.Preprocess ph) {
        placeholders.add(ph);
        return true;
    }

    @Override
    public @NotNull String parse(@NotNull Environment env, @NotNull String fullText) {
        for (Placeholder.Preprocess placeholder : placeholders) {
            fullText = placeholder.processPlaceholder(env, fullText);
        }
        return fullText;
    }

    @Override
    public @NotNull Collection<Placeholder.Preprocess> getPlaceholders() {
        return new HashSet<>(placeholders);
    }
}
