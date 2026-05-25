package fun.reactions.commands.impl.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaVariableSub extends RaCommandBase {
    public ReaVariableSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("variable")
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            help(ctx);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(literal("show") // TODO
                                /*.executes(ctx -> { show(ctx); return Command.SINGLE_SUCCESS; })*/)
                        .then(literal("create")
                                .executes(ctx -> {
                                    createVariable(ctx, "");
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(argument("value", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            createVariable(ctx, StringArgumentType.getString(ctx, "value"));
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(literal("delete") // TODO
                                /*.executes(ctx -> { delete(ctx); return Command.SINGLE_SUCCESS; })*/)
                        .then(literal("set") // TODO
                                /*.executes(ctx -> { set(ctx, ""); return Command.SINGLE_SUCCESS; })*/
                                .then(argument("value", StringArgumentType.greedyString()) // TODO
                                        /*.executes(ctx -> { set(ctx, StringArgumentType.getString(ctx, "value")); return Command.SINGLE_SUCCESS; })*/)))
                .build();
    }

    private void help(@NotNull CommandContext<CommandSourceStack> ctx) {
        sendHelp(ctx, "variable " + escape(StringArgumentType.getString(ctx, "name")),
                "create", "&e[value]", "Create variable with optional&e value",
                "show", "", "Show a variable",
                "delete", "", "Delete a variable",
                "set", "[value]", "Set variable to a &especified value"
        );
    }

    private void createVariable(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String value) {
        CommandSender sender = ctx.getSource().getSender();
        String name = StringArgumentType.getString(ctx, "name");
        String[] varName = name.split(":");
        platform.getPersistentVariables().setVariable(
                varName.length > 1 ? varName[0] : null,
                varName.length > 1 ? varName[1] : varName[0],
                value
        );
        sendPrefixed(sender, "Variable&a '" + escape(name) + "'&r was created with the value:\n" + escape(value));
    }

    private void show(@NotNull CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        String value = platform.getPersistentVariables().getVariable(null, StringArgumentType.getString(ctx, "name"));
        sender.sendMessage(value == null ? "" : value);
    }
}