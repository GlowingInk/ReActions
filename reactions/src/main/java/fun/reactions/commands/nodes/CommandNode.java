package fun.reactions.commands.nodes;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.commands.RaCommandException;
import fun.reactions.util.parameter.Parameters;
import me.lucko.commodore.Commodore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandNode implements Node, CommandExecutor, TabCompleter {
    private final String value;
    private final Executor defExecutor;
    private final List<Node> next;

    private CommandNode(String value, Executor defExecutor, List<Node> next) {
        this.value = value;
        this.defExecutor = defExecutor;
        this.next = next;
    }

    public static @NotNull CommandNode command(@NotNull Commodore commodore, @NotNull PluginCommand command, @NotNull Executor executor, @NotNull Node @NotNull ... next) {
        CommandNode node = new CommandNode(
                command.getName(),
                executor,
                Arrays.asList(next)
        );
        command.setExecutor(node);
        commodore.register(command, node.asBrigadier());
        return node;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        try {
            Map<String, String> paramsMap = new HashMap<>();
            String argsJoined = String.join(" ", args);
            paramsMap.put(COMMAND_KEY, cmd.getName());
            paramsMap.put(LABEL_KEY, label);
            paramsMap.put(FULL_COMMAND_KEY, label + " " + argsJoined);
            progress(paramsMap, argsJoined).accept(Parameters.fromMap(paramsMap), sender);
        } catch (RaCommandException exception) {
            sender.sendMessage(exception.message());
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return args.length == 0 ? List.of() : suggestions(String.join(" ", args));
    }

    @Override
    public @NotNull Executor progress(@NotNull Map<String, String> paramsBuilder, @NotNull String remaining) {
        for (Node piece : next) {
            Executor executor = piece.progress(paramsBuilder, remaining);
            if (executor != null) return executor;
        }
        return defExecutor;
    }

    @Override
    public @NotNull List<String> suggestions(@NotNull String remaining) {
        List<String> suggestions = new ArrayList<>();
        for (Node piece : next) {
            suggestions.addAll(piece.suggestions(remaining));
        }
        return suggestions;
    }

    @Override
    public @NotNull LiteralCommandNode<Object> asBrigadier() {
        LiteralArgumentBuilder<Object> builder = LiteralArgumentBuilder.literal(value);
        for (Node piece : next) {
            builder = builder.then(piece.asBrigadier());
        }
        return builder.build();
    }
}
