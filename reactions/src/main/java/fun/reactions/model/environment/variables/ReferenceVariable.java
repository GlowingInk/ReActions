package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public record ReferenceVariable(@NotNull Variable target) implements Variable {
    @Override
    public @NotNull String get() {
        return target.get();
    }

    @Override
    public @NotNull Map<String, Variable> children() {
        return target.children();
    }

    @Override
    public @Nullable Variable child(@NotNull String key) {
        return target.child(key);
    }

    @Override
    public @NotNull String resolve(@NotNull String params) {
        return target.resolve(params);
    }

    @Override
    public boolean putChild(@NotNull String key, @NotNull Variable value) {
        return target.putChild(key, value);
    }

    @Override
    public boolean setChild(@NotNull String path, @NotNull String value) {
        return target.setChild(path, value);
    }

    @Override
    public void markChanged() {
        target.markChanged();
    }

    @Override
    public @NotNull Variable set(@NotNull String value) {
        return target.set(value);
    }

    @Override
    public @NotNull Optional<String> changed() {
        return target.changed();
    }

    @Override
    public @NotNull Variable fork() {
        return new ReferenceVariable(target.fork());
    }
}
