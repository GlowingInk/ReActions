package fun.reactions.commands.nodes;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import fun.reactions.util.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DoubleArgNode implements Node {
    private final String name;
    private final Executor executor;

    private final Double min;
    private final Double max;
    private final List<Node> next;

    private DoubleArgNode(String name, Executor executor, Double min, Double max, List<Node> next) {
        this.name = name;
        this.executor = executor;
        this.min = min;
        this.max = max;
        this.next = next;
    }

    public static @NotNull DoubleArgNode doubleArg(@NotNull String name, @NotNull Node @NotNull ... next) {
        return doubleArg(name, null, next);
    }

    public static @NotNull DoubleArgNode doubleArg(@NotNull String name, double min, @NotNull Node @NotNull ... next) {
        return doubleArg(name, min, null, next);
    }

    public static @NotNull DoubleArgNode doubleArg(@NotNull String name, double min, double max, @NotNull Node @NotNull ... next) {
        return doubleArg(name, min, max, null, next);
    }

    public static @NotNull DoubleArgNode doubleArg(@NotNull String name, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new DoubleArgNode(name, executor, null, null, Arrays.asList(next));
    }

    public static @NotNull DoubleArgNode doubleArg(@NotNull String name, double min, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new DoubleArgNode(name, executor, min, null, Arrays.asList(next));
    }

    public static @NotNull DoubleArgNode doubleArg(@NotNull String name, double min, double max, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new DoubleArgNode(name, executor, min, max, Arrays.asList(next));
    }

    @Override
    public @Nullable Executor progress(@NotNull Map<String, String> paramsBuilder, @NotNull String remaining) {
        if (remaining.isEmpty()) {
            return null;
        }
        int index = remaining.indexOf(' ');
        String numStr = index == -1 ? remaining : remaining.substring(index);
        if (!NumberUtils.isNumber(remaining)) {
            return null;
        }
        double num;
        if (min == null || ((num = NumberUtils.asDouble(numStr)) >= min && (max == null || num <= max))) {
            remaining = remaining.substring(index + 1);
            paramsBuilder.put(name, numStr);
            for (Node piece : next) {
                Executor exec = piece.progress(paramsBuilder, remaining);
                if (exec != null) return exec;
            }
        }
        return executor;
    }

    @Override
    public @NotNull List<String> suggestions(@NotNull String remaining) {
        return List.of();
    }

    @Override
    public @NotNull ArgumentCommandNode<Object, Double> asBrigadier() {
        DoubleArgumentType type;
        if (min != null) {
            if (max != null) {
                type = DoubleArgumentType.doubleArg(min, max);
            } else {
                type = DoubleArgumentType.doubleArg(min);
            }
        } else {
            type = DoubleArgumentType.doubleArg();
        }
        var builder = RequiredArgumentBuilder.argument(name, type);
        for (Node piece : next) {
            builder = builder.then(piece.asBrigadier());
        }
        return builder.build();
    }
}
