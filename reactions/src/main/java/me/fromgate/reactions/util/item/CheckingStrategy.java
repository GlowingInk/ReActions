package me.fromgate.reactions.util.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public interface CheckingStrategy extends Predicate<String> {
    static @NotNull CheckingStrategy getStrategy(@Nullable String str, boolean regex) {
        if (str == null) {
            return Skip.INSTANCE;
        } else if (str.isEmpty()) {
            return Missing.INSTANCE;
        } else if (regex) {
            return new Regex(str);
        }
        return new Equal(str);
    }

    record Equal(@NotNull String str) implements CheckingStrategy {
        @Override
        public boolean test(@Nullable String str) {
            return this.str.equals(str);
        }
    }

    class Regex implements CheckingStrategy {
        private final Pattern pattern;

        public Regex(@NotNull String str) {
            pattern = Pattern.compile(str, Pattern.UNICODE_CASE);
        }

        @Override
        public boolean test(@Nullable String str) {
            return str != null && pattern.matcher(str).matches();
        }
    }

    class Missing implements CheckingStrategy {
        public static final Missing INSTANCE = new Missing();

        @Override
        public boolean test(@Nullable String str) {
            return str == null;
        }
    }

    class Skip implements CheckingStrategy {
        public static final Skip INSTANCE = new Skip();

        @Override
        public boolean test(@Nullable String s) {
            return true;
        }
    }
}
