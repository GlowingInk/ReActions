package fun.reactions.placeholders.resolvers;

import fun.reactions.logic.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class DynamicResolver implements Resolver<Placeholder.Dynamic> {
    private final List<Placeholder.Dynamic> placeholders = new ArrayList<>();

    @Override
    public boolean add(@NotNull Placeholder.Dynamic ph) {
        placeholders.add(ph);
        return true;
    }

    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String phText) {
        for (Placeholder.Dynamic ph : placeholders) {
            String result = ph.resolve(env, phText);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public @NotNull Collection<Placeholder.Dynamic> getPlaceholders() {
        return new HashSet<>(placeholders);
    }
}
