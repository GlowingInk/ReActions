package fun.reactions.commands.plugin.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActionsPlugin;
import fun.reactions.commands.plugin.RaCommandBase;
import fun.reactions.module.basic.ContextManager;
import fun.reactions.util.parameter.Parameters;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ExecCommand extends RaCommandBase {
    public static final String PARAMETERS_SYNTAX = "&e[player:<selector>] [delay:<time>]";

    public ExecCommand(@NotNull ReActionsPlugin plugin) {
        super(plugin);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("exec")
                .requires(permission("reactions.activator.execute"))
                .executes(ctx -> {
                    help(ctx);
                    return SINGLE_SUCCESS;
                })
                .then(argument("activator", StringArgumentType.word())
                        .suggests(suggestNames(() -> platform.getActivators().search().byType("FUNCTION").stream()
                                .map(activator -> activator.getLogic().getName())
                                .toList(), false))
                        .executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "activator"), ""))
                        .then(argument("parameters", StringArgumentType.greedyString())
                                .executes(ctx -> execute(
                                        ctx,
                                        StringArgumentType.getString(ctx, "activator"),
                                        StringArgumentType.getString(ctx, "parameters")
                                ))))
                .build();
    }

    private void help(@NotNull CommandContext<CommandSourceStack> ctx) {
        sendHelp(ctx,
                "", "<activator> " + PARAMETERS_SYNTAX + "&r", "Execute FUNCTION&a activator"
        );
    }

    // TODO Local vars
    public static int execute(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull String activator,
            @NotNull String rawParameters
    ) {
        boolean success = ContextManager.triggerFunction(
                ctx.getSource().getSender(),
                Parameters.fromString(rawParameters).with("activator", activator)
        );
        if (!success) {
            sendPrefixed(ctx, "Failed to execute &c'" + esc(activator) + "'&r - it doesn't exist or isn't a&e FUNCTION&r activator.");
        }
        return SINGLE_SUCCESS;
    }
}