package fun.reactions.util.bool;

import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.BooleanSupplier;

public enum TriBoolean {
    TRUE(TriState.TRUE), FALSE(TriState.FALSE), ANY(TriState.NOT_SET);

    private final TriState triState;

    TriBoolean(TriState triState) {
        this.triState = triState;
    }

    public @NotNull OptionalBoolean asOptional() {
        return switch (this) {
            case TRUE -> OptionalBoolean.TRUE;
            case FALSE -> OptionalBoolean.FALSE;
            case ANY -> OptionalBoolean.EMPTY;
        };
    }

    public @Nullable Boolean asBoolean() {
        return triState.toBoolean();
    }

    public boolean asBoolean(boolean def) {
        return triState.toBooleanOrElse(def);
    }

    public boolean asBoolean(@NotNull BooleanSupplier def) {
        return triState.toBooleanOrElseGet(def);
    }

    public boolean isValidFor(boolean bool) {
        return switch (this) {
            case TRUE -> bool;
            case FALSE -> !bool;
            case ANY -> true;
        };
    }

    public boolean isValidFor(@Nullable Boolean bool) {
        return switch (this) {
            case TRUE -> bool != null && bool;
            case FALSE -> bool != null && !bool;
            case ANY -> true;
        };
    }

    public @NotNull TriState asState() {
        return triState;
    }

    public static @NotNull TriBoolean byString(@Nullable String str) {
        if (str == null) return ANY;
        return switch (str.toUpperCase(Locale.ROOT)) {
            case "TRUE", "ON", "YES", "ALLOW", "ALLOWED", "ENABLE", "ENABLED" -> TRUE;
            case "FALSE", "OFF", "NO", "DENY", "DENIED", "DISABLE", "DISABLED" -> FALSE;
            default -> ANY;
        };
    }

    public static @NotNull TriBoolean byBoolean(@Nullable Boolean bool) {
        if (bool == Boolean.TRUE) return TRUE;
        if (bool == Boolean.FALSE) return FALSE;
        return ANY;
    }

    public static @NotNull TriBoolean byBoolean(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    public static @NotNull TriBoolean byState(@NotNull TriState triState) {
        return switch (triState) {
            case TRUE -> TRUE;
            case FALSE -> FALSE;
            default -> ANY;
        };
    }
    
    public static final class Mapper {
        private final String trueMain;
        private final Set<String> trueVariants;
        private final String falseMain;
        private final Set<String> falseVariants;
        private final String anyMain;
        private final Set<String> anyVariants;
        private final TriBoolean def;

        private Mapper(
                String trueMain, Set<String> trueVariants, 
                String falseMain, Set<String> falseVariants, 
                String anyMain, Set<String> anyVariants, 
                TriBoolean def
        ) {
            this.trueMain = trueMain;
            this.trueVariants = trueVariants;
            this.falseMain = falseMain;
            this.falseVariants = falseVariants;
            this.anyMain = anyMain;
            this.anyVariants = anyVariants;
            this.def = def;
        }

        public @NotNull TriBoolean byString(@Nullable String str) {
            if (str == null) return def;
            str = str.toUpperCase(Locale.ROOT);
            if (trueVariants.contains(str)) {
                return TriBoolean.TRUE;
            }
            if (falseVariants.contains(str)) {
                return TriBoolean.FALSE;
            }
            if (anyVariants.contains(str)) {
                return TriBoolean.ANY;
            }
            return def;
        }
        
        public @NotNull String toString(@NotNull TriBoolean triBoolean) {
            return switch (triBoolean) {
                case TRUE -> trueMain;
                case FALSE -> falseMain;
                case ANY -> anyMain;
            };
        }
        
        public static class Builder {
            private String trueMain;
            private final Set<String> trueVariants;
            private String falseMain;
            private final Set<String> falseVariants;
            private String anyMain;
            private final Set<String> anyVariants;
            private TriBoolean def;
            
            public Builder() {
                trueMain = "TRUE";
                trueVariants = new HashSet<>();
                falseMain = "FALSE";
                falseVariants = new HashSet<>();
                anyMain = "ANY";
                anyVariants = new HashSet<>();
                def = ANY;
            }

            @Contract("_ -> this")
            public @NotNull Builder trueMain(@NotNull String trueMain) {
                this.trueMain = trueMain.toUpperCase(Locale.ROOT);
                return this;
            }

            @Contract("_ -> this")
            public @NotNull Builder addTrueVariants(@NotNull String... trueVariants) {
                for (String variant : trueVariants) {
                    this.trueVariants.add(variant.toUpperCase(Locale.ROOT));
                }
                return this;
            }

            @Contract("-> this")
            public @NotNull Builder clearTrueVariants() {
                this.trueVariants.clear();
                return this;
            }

            @Contract("_ -> this")
            public @NotNull Builder falseMain(@NotNull String falseMain) {
                this.falseMain = falseMain.toUpperCase(Locale.ROOT);
                return this;
            }

            @Contract("_ -> this")
            public @NotNull Builder addFalseVariants(@NotNull String... falseVariants) {
                for (String variant : falseVariants) {
                    this.falseVariants.add(variant.toUpperCase(Locale.ROOT));
                }
                return this;
            }

            @Contract("-> this")
            public @NotNull Builder clearFalseVariants() {
                this.falseVariants.clear();
                return this;
            }

            @Contract("_ -> this")
            public @NotNull Builder anyMain(@NotNull String anyMain) {
                this.anyMain = anyMain.toUpperCase(Locale.ROOT);
                return this;
            }

            @Contract("_ -> this")
            public @NotNull Builder addAnyVariants(@NotNull String... anyVariants) {
                for (String variant : anyVariants) {
                    this.anyVariants.add(variant.toUpperCase(Locale.ROOT));
                }
                return this;
            }

            @Contract("-> this")
            public @NotNull Builder clearAnyVariants() {
                this.anyVariants.clear();
                return this;
            }

            @Contract("_ -> this")
            public @NotNull Builder def(@NotNull TriBoolean def) {
                this.def = def;
                return this;
            }

            @Contract(value = "-> new", pure = true)
            public @NotNull Mapper build() {
                return new Mapper(
                        trueMain, Set.copyOf(trueVariants),
                        falseMain, Set.copyOf(falseVariants),
                        anyMain, Set.copyOf(anyVariants),
                        def
                );
            }
        }
    }
}
