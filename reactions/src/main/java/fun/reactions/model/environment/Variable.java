package fun.reactions.model.environment;

import fun.reactions.util.function.SafeSupplier;
import fun.reactions.util.parameter.Parameterizable;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface Variable {
    Variable EMPTY = new Variable() {
        @Override
        public @NotNull String get() {
            return "";
        }

        @Override
        public @NotNull Variable set(@NotNull String value) {
            return Variable.of(value, true);
        }

        @Override
        public @NotNull Optional<String> changed() {
            return Optional.empty();
        }

        @Override
        public @NotNull Variable fork() {
            return this;
        }
    };

    /**
     * Current string value of this variable, e.g. the value shown for a bare
     * {@code %[key:param]}/{@code %[local:key:param]}.
     */
    @NotNull String get();

    /**
     * Named facets of this variable, keyed case-insensitively (e.g. a block variable exposing
     * {@code x}/{@code y}/{@code location}). Override alongside {@link #child} to make a variable
     * chainable, Lua-table-style. Empty by default - a plain-value variable has no facets.
     */
    default @NotNull Map<@NotNull String, @NotNull Variable> children() {
        return Map.of();
    }

    /**
     * Look up a single named facet of this variable, or {@code null} if it doesn't have one by
     * that name. The default implementation is a case-insensitive lookup into {@link #children()}.
     */
    default @Nullable Variable child(@NotNull String key) {
        return children().get(key);
    }

    /**
     * Resolve a colon-separated path against this variable, e.g. {@code block:location:world}
     * resolves {@code location:world} against the {@code block} variable's {@code location} child,
     * one path segment at a time, until either the path is exhausted or a segment has no matching
     * child - in which case this variable's own {@link #get()} is returned as a fallback. The
     * segment {@code self} is reserved: it always returns this variable's own string form
     * ({@link #get()} for a leaf, {@link #asParameters()} for anything with children), regardless
     * of whether a real child happens to be named {@code self}.
     */
    default @NotNull String resolve(@NotNull String params) {
        if (params.isEmpty()) return get();
        int index = params.indexOf(':');
        String key = index == -1 ? params : params.substring(0, index);
        if (key.equalsIgnoreCase("self")) return selfString();
        Variable next = child(key);
        if (next == null) return get();
        return index == -1 ? next.get() : next.resolve(params.substring(index + 1));
    }

    private @NotNull String selfString() {
        return children().isEmpty() ? get() : asParameters().toString();
    }

    /**
     * A {@link Parameters} view built from {@link #children()}: one entry per child, using its
     * string form, or its own {@code #asParameters()} recursively if it's itself a
     * {@link Parameterizable}. Returns {@link Parameters#EMPTY} for a variable with no children.
     */
    default @NotNull Parameters asParameters() {
        Map<String, Variable> children = children();
        if (children.isEmpty()) return Parameters.EMPTY;
        Map<String, String> map = new HashMap<>(children.size());
        for (var entry : children.entrySet()) {
            Variable child = entry.getValue();
            map.put(entry.getKey(), child instanceof Parameterizable p ? p.asParameters().toString() : child.get());
        }
        return Parameters.fromMap(map);
    }

    /**
     * Store a child under a single key (e.g. the {@code x} of {@code block:x}). No-op, returning
     * {@code false}, for variables that don't keep a mutable children store - override alongside
     * {@link #children()}/{@link #child} to support it.
     *
     * @return whether the child was actually stored
     */
    default boolean putChild(@NotNull String key, @NotNull Variable value) {
        return false;
    }

    /**
     * Write-side mirror of {@link #resolve}: walks existing children one path segment at a time,
     * then {@link #putChild}s at the leaf. Does not create missing intermediate nodes - a path
     * through a facet that doesn't exist yet simply fails (returns {@code false}) rather than
     * auto-vivifying one. Marks this variable (and every intermediate node walked through, not
     * just the leaf) as {@link #changed} on success, via {@link #markChanged}.
     *
     * @return whether the value was actually applied
     */
    default boolean setChild(@NotNull String path, @NotNull String value) {
        int index = path.indexOf(':');
        boolean applied;
        if (index == -1) {
            applied = putChild(path, Variable.of(value, true));
        } else {
            Variable next = child(path.substring(0, index));
            applied = next != null && next.setChild(path.substring(index + 1), value);
        }
        if (applied) markChanged();
        return applied;
    }

    /**
     * Hook invoked by {@link #setChild} (and by implementations' own {@link #putChild}) whenever
     * this variable was actually mutated. No-op by default; override alongside {@link #changed} to
     * track it.
     */
    default void markChanged() {
    }

    /**
     * Replace this variable's value outright. Implementations may mutate themselves in place and
     * return {@code this} (preserving any children), or return a different {@link Variable}
     * entirely to replace themselves wherever they're stored (e.g. in a {@link
     * fun.reactions.model.environment.Variables} map) - callers must use the returned value, not
     * assume identity is preserved.
     */
    @NotNull Variable set(@NotNull String value);

    /**
     * Whether this variable was explicitly {@link #set}/{@link #setChild} since it was created,
     * and if so, its current value - mirroring what {@link #get()} would return. Used to tell
     * "an action explicitly overrode this" apart from "still holds its seeded/default value",
     * e.g. by listeners deciding whether to override a Bukkit event's own field.
     */
    @NotNull Optional<String> changed();

    /**
     * An independent copy of this variable, safe to mutate without affecting the original - needed
     * whenever an {@link fun.reactions.model.environment.Environment} is forked for a recursive or
     * nested activation (see {@code RunFunctionAction}, {@code FunctionActivator}). Variables with
     * no mutable state of their own (no children store, nothing {@link #changed}) may return
     * {@code this} instead of actually copying.
     */
    @NotNull Variable fork();

    @ApiStatus.Internal
    static @NotNull Variable of(@NotNull String value, boolean changed) {
        return new Value(value, changed);
    }

    /**
     * A plain, untracked string variable - {@link #changed()} stays empty until it's explicitly
     * {@link #set}.
     */
    static @NotNull Variable simple(@NotNull String value) {
        return of(value, false);
    }

    static @NotNull Variable simple(@NotNull Enum<?> value) {
        return simple(value.name());
    }

    static @NotNull Variable simple(@NotNull Object value) {
        return simple(value.toString());
    }

    /**
     * Same as {@link #simple}; the separate name only documents intent at the call site - typically
     * a value a listener may later check via {@link #changed()} to see if an action overrode it.
     */
    static @NotNull Variable property(@NotNull String value) {
        return of(value, false);
    }

    static @NotNull Variable property(@NotNull Enum<?> value) {
        return property(value.name());
    }

    static @NotNull Variable property(@NotNull Object value) {
        return property(value.toString());
    }

    /**
     * A variable whose value is computed on demand from {@code value} rather than stored up front -
     * for data that's cheap to defer but possibly expensive/unnecessary to compute if never read.
     */
    static @NotNull Variable lazy(@NotNull SafeSupplier<String> value) {
        return new Lazy(value);
    }

    @ApiStatus.Internal
    class Value implements Variable {
        private String value;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<String> optional; // We're expecting Value variable to be checked, so just cache optional

        Value(@NotNull String value, boolean changed) {
            this.value = value;
            this.optional = changed ? Optional.of(value) : Optional.empty();
        }

        @Override
        public @NotNull String get() {
            return value;
        }

        @Override
        public @NotNull Variable set(@NotNull String value) {
            this.value = value;
            this.optional = Optional.of(value);
            return this;
        }

        @Override
        public @NotNull Optional<String> changed() {
            return optional;
        }

        @Override
        public @NotNull Variable fork() {
            return new Value(value, false);
        }
    }

    @ApiStatus.Internal
    record Lazy(@NotNull SafeSupplier<String> getter) implements Variable {
        @Override
        public @NotNull String get() {
            return getter.get();
        }

        @Override
        public @NotNull Variable set(@NotNull String value) {
            return Variable.of(value, true);
        }

        @Override
        public @NotNull Optional<String> changed() {
            return Optional.empty();
        }

        @Override
        public @NotNull Variable fork() {
            return this;
        }
    }
}
