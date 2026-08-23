package fun.reactions.commands.plugin.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActionsPlugin;
import fun.reactions.commands.plugin.RaCommandBase;
import fun.reactions.module.basic.ContextManager;
import fun.reactions.util.parameter.Parameters;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ExecCommand extends RaCommandBase {
    public ExecCommand(@NotNull ReActionsPlugin plugin) {
        super(plugin);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("exec")
                .requires(permission("reactions.exec"))
                .executes(ctx -> {
                    help(ctx);
                    return Command.SINGLE_SUCCESS;
                })
                .then(argument("activator", StringArgumentType.word())
                        .executes(ctx -> {
                            activate(ctx, "");
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(argument("parameters", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    activate(ctx, StringArgumentType.getString(ctx, "parameters"));
                                    return Command.SINGLE_SUCCESS;
                                })))
                .build();
    }

    private void help(@NotNull CommandContext<CommandSourceStack> ctx) {
        sendHelp(ctx,
                "", "<activator> &e[player:<selector>] [delay:<time>]&r", "Execute FUNCTION&a activator&r."
        );
    }

    private void activate(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String rawParameters) {
        ContextManager.triggerFunction(
                ctx.getSource().getSender(),
                Parameters.fromString(rawParameters).with("activator", StringArgumentType.getString(ctx, "activator"))
        );
    }
}