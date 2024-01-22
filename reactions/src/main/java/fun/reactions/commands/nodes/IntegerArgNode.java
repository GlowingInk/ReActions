package fun.reactions.commands.nodes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import fun.reactions.util.num.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class IntegerArgNode implements Node {
    private final String name;
    private final Executor executor;

    private final Range range;
    private final List<Node> next;

    private IntegerArgNode(String name, Executor executor, Range range, List<Node> next) {
        this.name = name;
        this.executor = executor;
        this.range = range;
        this.next = next;
    }

    public static @NotNull IntegerArgNode integerArg(@NotNull String name, @NotNull Node @NotNull ... next) {
        return integerArg(name, (Executor) null, next);
    }

    public static @NotNull IntegerArgNode integerArg(@NotNull String name, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new IntegerArgNode(name, executor, Range.intUnlimited(), Arrays.asList(next));
    }

    public static @NotNull IntegerArgNode integerArg(@NotNull String name, @NotNull IntegerArgNode.Range range, @NotNull Node @NotNull ... next) {
        return integerArg(name, range, null, next);
    }

    public static @NotNull IntegerArgNode integerArg(@NotNull String name, @NotNull IntegerArgNode.Range range, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new IntegerArgNode(name, executor, range, Arrays.asList(next));
    }

    @Override
    public @Nullable Executor progress(@NotNull Map<String, String> paramsBuilder, @NotNull String remaining) {
        if (remaining.isEmpty()) {
            return null;
        }
        int index = remaining.indexOf(' ');
        String numStr = index == -1 ? remaining : remaining.substring(index);
        if (!range.isValidFor(numStr)) {
            return null;
        }
        remaining = remaining.substring(index + 1);
        paramsBuilder.put(name, numStr);
        for (Node piece : next) {
            Executor exec = piece.progress(paramsBuilder, remaining);
            if (exec != null) return exec;
        }
        return executor;
    }

    @Override
    public @NotNull List<String> suggestions(@NotNull String remaining) {
        return List.of();
    }

    @Override
    public @NotNull ArgumentCommandNode<Object, Integer> asBrigadier() {
        var builder = RequiredArgumentBuilder.argument(name, range.asType());
        for (Node piece : next) {
            builder = builder.then(piece.asBrigadier());
        }
        return builder.build();
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    public static class Range {
        private static final Range EMPTY = new Range(null, null);

        private final Integer min;
        private final Integer max;

        private Range(@Nullable Integer min, @Nullable Integer max) {
            this.min = min;
            this.max = max;
        }

        public static @NotNull IntegerArgNode.Range intUnlimited() {
            return EMPTY;
        }

        public static @NotNull IntegerArgNode.Range intFrom(int min) {
            return new Range(min, null);
        }

        public static @NotNull IntegerArgNode.Range intRange(int min, int max) {
            return new Range(min, max);
        }

        public boolean isValidFor(@NotNull String numStr) {
            OptionalInt numOpt = NumberUtils.parseInteger(numStr);
            if (numOpt.isEmpty()) {
                return false;
            }
            int num = numOpt.getAsInt();
            return min == null || (num >= min && (max == null || num <= max));
        }

        public @NotNull IntegerArgumentType asType() {
            if (min != null) {
                if (max != null) {
                    return IntegerArgumentType.integer(min, max);
                } else {
                    return IntegerArgumentType.integer(min);
                }
            } else {
                return IntegerArgumentType.integer();
            }
        }
    }
}
