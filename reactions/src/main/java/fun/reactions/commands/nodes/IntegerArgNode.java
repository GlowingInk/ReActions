package fun.reactions.commands.nodes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import fun.reactions.util.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IntegerArgNode implements Node {
    private final String name;
    private final Executor executor;

    private final Integer min;
    private final Integer max;
    private final List<Node> next;

    private IntegerArgNode(String name, Executor executor, Integer min, Integer max, List<Node> next) {
        this.name = name;
        this.executor = executor;
        this.min = min;
        this.max = max;
        this.next = next;
    }

    public static @NotNull IntegerArgNode integerArg(@NotNull String name, @NotNull Node @NotNull ... next) {
        return integerArg(name, null, next);
    }

    public static @NotNull IntegerArgNode integerArg(@NotNull String name, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new IntegerArgNode(name, executor, null, null, Arrays.asList(next));
    }

    public static @NotNull IntegerArgNode integerArg(@NotNull String name, int min, @NotNull Node @NotNull ... next) {
        return integerArg(name, min, null, next);
    }

    public static @NotNull IntegerArgNode integerArg(@NotNull String name, int min, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new IntegerArgNode(name, executor, min, null, Arrays.asList(next));
    }

    public static @NotNull IntegerArgNode integerArg(@NotNull String name, int min, int max, @NotNull Node @NotNull ... next) {
        return integerArg(name, min, max, null, next);
    }

    public static @NotNull IntegerArgNode integerArg(@NotNull String name, int min, int max, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new IntegerArgNode(name, executor, min, max, Arrays.asList(next));
    }

    @Override
    public @Nullable Executor progress(@NotNull Map<String, String> paramsBuilder, @NotNull String remaining) {
        if (remaining.isEmpty()) {
            return null;
        }
        int index = remaining.indexOf(' ');
        String numStr = index == -1 ? remaining : remaining.substring(index);
        if (!NumberUtils.isNumber(remaining, NumberUtils.Is.INTEGER)) {
            return null;
        }
        int num;
        if ((min == null || ((num = NumberUtils.asInteger(numStr)) >= min && (max == null || num <= max)))) {
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
    public @NotNull ArgumentCommandNode<Object, Integer> asBrigadier() {
        IntegerArgumentType type;
        if (min != null) {
            if (max != null) {
                type = IntegerArgumentType.integer(min, max);
            } else {
                type = IntegerArgumentType.integer(min);
            }
        } else {
            type = IntegerArgumentType.integer();
        }
        var builder = RequiredArgumentBuilder.argument(name, type);
        for (Node piece : next) {
            builder = builder.then(piece.asBrigadier());
        }
        return builder.build();
    }

    @Override
    public @NotNull String getName() {
        return name;
    }
}
