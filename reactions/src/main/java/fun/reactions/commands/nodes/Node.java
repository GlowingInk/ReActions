package fun.reactions.commands.nodes;

import com.mojang.brigadier.tree.CommandNode;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public interface Node {
    String COMMAND_KEY = "full-command";

    @Contract(mutates = "param1")
    @Nullable Executor progress(@NotNull Map<String, String> paramsBuilder, @NotNull String remaining);

    @NotNull List<String> suggestions(@NotNull String remaining);

    @NotNull CommandNode<Object> asBrigadier();

    @FunctionalInterface
    interface Executor extends BiConsumer<Parameters, CommandSender> {}
}
