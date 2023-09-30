package fun.reactions.commands.nodes;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LiteralNode implements Node {
    private final String value;
    private final Executor executor;
    private final List<Node> next;

    private final List<String> suggestions;

    public LiteralNode(String value, Executor executor, List<Node> next) {
        this.value = value;
        this.executor = executor;
        this.next = next;
        this.suggestions = List.of(value);
    }

    public static LiteralNode literal(@NotNull String value, @Nullable Executor executor, @NotNull Node @NotNull ... next) {
        return new LiteralNode(
                value,
                executor,
                List.of(next)
        );
    }

    public static LiteralNode literal(@NotNull String value, @NotNull Node @NotNull ... next) {
        return literal(value, null, next);
    }

    @Override
    public @Nullable Executor progress(@NotNull Map<String, String> paramsBuilder, @NotNull String remaining) {
        if (remaining.equals(value)) {
            return executor;
        } else if (remaining.startsWith(value + ' ')) {
            remaining = remaining.substring(value.length() + 1);
            for (Node piece : next) {
                Executor exec = piece.progress(paramsBuilder, remaining);
                if (exec != null) return exec;
            }
            return executor;
        }
        return null;
    }

    @Override
    public @NotNull List<String> suggestions(@NotNull String remaining) {
        int index = remaining.indexOf(' ');
        if (index == -1) {
            if (value.startsWith(remaining)) {
                return suggestions;
            } else {
                return List.of();
            }
        } else if (remaining.substring(0, index).equals(value)) {
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
    public @NotNull LiteralCommandNode<Object> asBrigadier() {
        LiteralArgumentBuilder<Object> builder = LiteralArgumentBuilder.literal(value);
        for (Node piece : next) {
            builder = builder.then(piece.asBrigadier());
        }
        return builder.build();
    }

    @Override
    public @NotNull String getName() {
        return value;
    }
}
