package fun.reactions.model.environment;

import fun.reactions.util.function.FunctionalUtils;
import fun.reactions.util.function.SafeSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Variable {
    Variable EMPTY = new Variable() {
        @Override
        public @NotNull String get() {
            return "";
        }

        @Override
        public @NotNull Variable set(@NotNull String value) {
            return new Simple(value);
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

    static @NotNull Variable simple(@NotNull String value) {
        return new Simple(value);
    }

    static @NotNull Variable simple(@NotNull Enum<?> value) {
        return simple(value.name());
    }

    static @NotNull Variable simple(@NotNull Object value) {
        return simple(value.toString());
    }

    static @NotNull Variable property(@NotNull String value) {
        return new Property(value);
    }

    static @NotNull Variable property(@NotNull Enum<?> value) {
        return property(value.name());
    }

    static @NotNull Variable property(@NotNull Object value) {
        return property(value.toString());
    }

    static @NotNull Variable lazy(@NotNull SafeSupplier<String> value) {
        return new Lazy(value);
    }

    // TODO Probably make those immutable instead for better data flow control

    class Simple implements Variable {
        private String value;

        public Simple(@NotNull String value) {
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
            return new Simple(value);
        }
    }

    class Property implements Variable {
        private String value;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<String> optional; // We're expecting Property variable to be checked, so just cache optional

        public Property(@NotNull String value) {
            this.value = value;
            this.optional = Optional.empty();
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
        public @NotNull Optional<String> getChanged() {
            return optional;
        }

        @Override
        public @NotNull Variable fork() {
            return new Simple(value);
        }
    }

    class Lazy implements Variable {
        private final SafeSupplier<String> getter;

        public Lazy(@NotNull SafeSupplier<String> getter) {
            this.getter = FunctionalUtils.asSafeCaching(getter);
        }

        @Override
        public @NotNull String get() {
            return getter.get();
        }

        @Override
        public @NotNull Variable set(@NotNull String value) {
           return new Simple(value);
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
