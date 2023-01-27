package me.fromgate.reactions.logic.context;

import me.fromgate.reactions.util.function.FunctionalUtils;
import me.fromgate.reactions.util.function.SafeSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public interface Variable {
    Variable EMPTY = new Variable() {
        @Override
        public @NotNull String get() {
            return "";
        }

        @Override
        public @NotNull Variable set(@NotNull String value) {
            return new Plain(value);
        }

        @Override
        public @NotNull Optional<String> getChanged() {
            return Optional.empty();
        }

        @Override
        public @NotNull Variable fork() {
            return this;
        }
    };

    @NotNull String get();

    @NotNull Variable set(@NotNull String value);

    @NotNull Optional<String> getChanged();

    @NotNull Variable fork();

    static @NotNull Variable plain(@NotNull String value) {
        return new Plain(value);
    }

    static @NotNull Variable plain(@NotNull Enum<?> value) {
        return new Plain(value.name());
    }

    static @NotNull Variable plain(@NotNull Object value) {
        return new Plain(String.valueOf(value));
    }

    static @NotNull Variable property(@NotNull String value) {
        return new Property(value);
    }

    static @NotNull Variable property(@NotNull Enum<?> value) {
        return new Property(value.name());
    }

    static @NotNull Variable property(@NotNull Object value) {
        return new Property(String.valueOf(value));
    }

    static @NotNull Variable lazy(@NotNull SafeSupplier<String> value) {
        return new Lazy(value);
    }

    static @NotNull Map<String, Variable> plainMap(@NotNull Map<String, String> origin) {
        Map<String, Variable> vars = new HashMap<>(origin.size());
        for (var entry : origin.entrySet()) {
            vars.put(entry.getKey(), plain(entry.getValue()));
        }
        return vars;
    }

    class Plain implements Variable {
        private String value;

        public Plain(@NotNull String value) {
            this.value = value;
        }

        @Override
        public @NotNull String get() {
            return value;
        }

        @Override
        public @NotNull Variable set(@NotNull String value) {
            this.value = value;
            return this;
        }

        @Override
        public @NotNull Optional<String> getChanged() {
            return Optional.of(value);
        }

        @Override
        public @NotNull Variable fork() {
            return new Plain(value);
        }
    }

    class Property implements Variable {
        private String value;
        private boolean changed;

        public Property(@NotNull String value) {
            this.value = value;
            this.changed = false;
        }

        @Override
        public @NotNull String get() {
            return value;
        }

        @Override
        public @NotNull Variable set(@NotNull String value) {
            this.value = value;
            this.changed = true;
            return this;
        }

        @Override
        public @NotNull Optional<String> getChanged() {
            return changed ? Optional.of(value) : Optional.empty();
        }

        @Override
        public @NotNull Variable fork() {
            return new Plain(value);
        }
    }

    class Lazy implements Variable {
        private final Supplier<String> getter;

        public Lazy(@NotNull SafeSupplier<String> getter) {
            this.getter = FunctionalUtils.asSafeCaching(getter);
        }

        @Override
        public @NotNull String get() {
            return getter.get();
        }

        @Override
        public @NotNull Variable set(@NotNull String value) {
           return new Plain(value);
        }

        @Override
        public @NotNull Optional<String> getChanged() {
            return Optional.empty();
        }

        @Override
        public @NotNull Variable fork() {
            return this;
        }
    }
}
