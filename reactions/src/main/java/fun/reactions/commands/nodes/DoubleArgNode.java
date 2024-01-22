package fun.reactions.commands.nodes;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import fun.reactions.util.num.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DoubleArgNode implements Node {
    private final String name;
    private final Executor executor;

    private final Range range;
    private final List<Node> next;

    private DoubleArgNode(String name, Executor executor, Range range, List<Node> next) {
        this.name = name;
        this.executor = executor;
        this.range = range;
        this.next = next;
    }

    public static @NotNull DoubleArgNode doubleArg(@NotNull String name, @NotNull Node @NotNull ... next) {
        return doubleArg(name, (Executor) null, next);
    }

    public static @NotNull DoubleArgNode doubleArg(@NotNull String name, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new DoubleArgNode(name, executor, Range.doubleUnlimited(), Arrays.asList(next));
    }

    public static @NotNull DoubleArgNode doubleArg(@NotNull String name, @NotNull Range range, @NotNull Node @NotNull ... next) {
        return doubleArg(name, range, null, next);
    }

    public static @NotNull DoubleArgNode doubleArg(@NotNull String name, @NotNull Range range, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new DoubleArgNode(name, executor, range, Arrays.asList(next));
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
    public @NotNull ArgumentCommandNode<Object, Double> asBrigadier() {
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

        private final Double min;
        private final Double max;

        private Range(@Nullable Double min, @Nullable Double max) {
            this.min = min;
            this.max = max;
        }

        public static @NotNull DoubleArgNode.Range doubleUnlimited() {
            return EMPTY;
        }

        public static @NotNull DoubleArgNode.Range doubleFrom(double min) {
            return new Range(min, null);
        }

        public static @NotNull DoubleArgNode.Range doubleRange(double min, double max) {
            return new Range(min, max);
        }

        public boolean isValidFor(@NotNull String numStr) {
            if (!NumberUtils.isNumber(numStr)) {
                return false;
            }
            double num;
            return min == null || ((num = NumberUtils.asDouble(numStr)) >= min && (max == null || num <= max));
        }

        public @NotNull DoubleArgumentType asType() {
            if (min != null) {
                if (max != null) {
                    return DoubleArgumentType.doubleArg(min, max);
                } else {
                    return DoubleArgumentType.doubleArg(min);
                }
            } else {
                return DoubleArgumentType.doubleArg();
            }
        }
    }
}
