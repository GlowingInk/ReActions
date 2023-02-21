package fun.reactions.placeholders.resolvers;

import fun.reactions.logic.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class PreprocessResolver implements Resolver<Placeholder.Preprocess> {
    private final List<Placeholder.Preprocess> placeholders = new ArrayList<>();

    @Override
    public boolean add(@NotNull Placeholder.Preprocess ph) {
        placeholders.add(ph);
        return true;
    }

    @Override
    public @NotNull String resolve(@NotNull Environment env, @NotNull String fullText) {
        for (Placeholder.Preprocess placeholder : placeholders) {
            fullText = placeholder.resolveAll(env, fullText);
        }
        return fullText;
    }

    @Override
    public @NotNull Collection<Placeholder.Preprocess> getPlaceholders() {
        return new HashSet<>(placeholders);
    }
}
