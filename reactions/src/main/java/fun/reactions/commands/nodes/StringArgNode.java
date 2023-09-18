package fun.reactions.commands.nodes;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StringArgNode implements Node {
    protected final String name;
    private final Type type;
    private final Supplier<Collection<String>> suggestions;
    private final Executor executor;
    private final List<Node> next;

    public StringArgNode(String name, Type type, Executor executor, Supplier<Collection<String>> suggestions, List<Node> next) {
        this.name = name;
        this.type = type;
        this.executor = executor;
        this.suggestions = suggestions;
        this.next = next;
    }

    public static StringArgNode stringArg(
            @NotNull String name,
            @NotNull Type type,
            @Nullable Supplier<Collection<String>> suggestions,
            @Nullable Executor executor,
            @NotNull Node @NotNull ... next
    ) {
        return new StringArgNode(
                name,
                type,
                executor,
                suggestions,
                List.of(next)
        );
    }

    public static StringArgNode stringArg(
            @NotNull String name,
            @NotNull Type type,
            @Nullable Executor executor,
            @NotNull Node @NotNull ... next
    ) {
        return stringArg(name, type, null, executor, next);
    }

    public static StringArgNode stringArg(
            @NotNull String name,
            @NotNull Type type,
            @Nullable Supplier<Collection<String>> suggestions,
            @NotNull Node @NotNull ... next
    ) {
        return stringArg(name, type, suggestions, null, next);
    }

    public static StringArgNode stringArg(@NotNull String name, @NotNull Type type, @NotNull Node @NotNull ... next) {
        return stringArg(name, type, null, null, next);
    }

    @Override
    public @Nullable Executor progress(@NotNull Map<String, String> paramsBuilder, @NotNull String remaining) {
        if (remaining.isEmpty()) {
            if (type == Type.OPTIONAL_GREEDY) {
                paramsBuilder.put(name, remaining);
                return executor;
            }
            return null;
        }
        int index;
        switch (type) {
            case QUOTED: {
                char start = remaining.charAt(0);
                if (start == '\'' || start == '\"') {
                    index = remaining.indexOf(start, 1);
                    if (index == -1) return null;
                    int next = index + 1;
                    if (remaining.length() == next || remaining.charAt(next) != ' ') return executor;
                    break;
                }
            }
            case WORD: {
                index = remaining.indexOf(' ');
                if (index == -1) return executor;
                paramsBuilder.put(name, remaining.substring(0, index));
                break;
            }
            default: {
                paramsBuilder.put(name, remaining);
                return executor;
            }
        }
        paramsBuilder.put(name, remaining.substring(0, index));
        remaining = remaining.substring(index + 1);
        for (Node piece : next) {
            Executor exec = piece.progress(paramsBuilder, remaining);
            if (exec != null) return exec;
        }
        return executor;
    }

    @Override
    public @NotNull List<String> suggestions(@NotNull String remaining) {
        int index = remaining.indexOf(' ');
        if (index == -1) {
            if (suggestions != null) {
                return StringUtil.copyPartialMatches(remaining, suggestions.get(), new ArrayList<>());
            }
        } else {
            remaining = remaining.substring(index + 1);
            List<String> suggestions = new ArrayList<>();
            for (Node piece : next) {
                suggestions.addAll(piece.suggestions(remaining));
            }
            return suggestions;
        }
        return List.of();
    }

    @Override
    public @NotNull ArgumentCommandNode<Object, String> asBrigadier() {
        var builder = RequiredArgumentBuilder.argument(name, type.get());
        for (Node piece : next) {
            builder = builder.then(piece.asBrigadier());
        }
        return builder.build();
    }

    public enum Type implements Supplier<StringArgumentType> {
        WORD, QUOTED, GREEDY, OPTIONAL_GREEDY;

        @Override
        public @NotNull StringArgumentType get() {
            return switch (this) {
                case WORD -> StringArgumentType.word();
                case QUOTED -> StringArgumentType.string();
                case GREEDY, OPTIONAL_GREEDY -> StringArgumentType.greedyString();
            };
        }
    }
}
