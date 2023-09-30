package fun.reactions.commands.nodes;

import com.mojang.brigadier.tree.CommandNode;
import fun.reactions.util.naming.Named;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

// TODO Aliases registration
public interface Node extends Named {
    String COMMAND_KEY = "command";
    String FULL_COMMAND_KEY = "full-command";
    String LABEL_KEY = "label";

    @Contract(mutates = "param1")
    @Nullable Executor progress(@NotNull Map<String, String> paramsBuilder, @NotNull String remaining);

    @NotNull List<String> suggestions(@NotNull String remaining);

    @NotNull CommandNode<Object> asBrigadier();

    @FunctionalInterface
    interface Executor extends BiConsumer<Parameters, CommandSender> {}
}
