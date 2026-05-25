package fun.reactions.commands.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActionsPlugin;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.commands.impl.sub.ReaActivatorSub;
import fun.reactions.commands.impl.sub.ReaCreateSub;
import fun.reactions.commands.impl.sub.ReaLocationSub;
import fun.reactions.commands.impl.sub.ReaVariableSub;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReactionsCommand extends RaCommandBase {
    public ReactionsCommand(@NotNull ReActionsPlugin plugin) {
        super(plugin);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("reactions")
                .executes(ctx -> {
                    help(ctx);
                    return Command.SINGLE_SUCCESS;
                })
                .then(new ReaCreateSub(platform).asNode())
                .then(new ReaActivatorSub(platform).asNode())
                .then(new ReaLocationSub(platform).asNode())
                .then(new ReaVariableSub(platform).asNode())
                .then(menuNode())
                .then(listNode())
                .then(reloadNode())
                .build();
    }

    private @NotNull LiteralCommandNode<CommandSourceStack> menuNode() {
        return literal("menu")
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage("menu help");
                    return Command.SINGLE_SUCCESS;
                })
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage("menu specific help");
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(literal("title")
                                .then(argument("title", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ctx.getSource().getSender().sendMessage("set title");
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(literal("open")
                                .executes(ctx -> {
                                    ctx.getSource().getSender().sendMessage("menu open");
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(argument("players", ArgumentTypes.players())
                                        .executes(ctx -> {
                                            ctx.getSource().getSender().sendMessage("menu open for others");
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(literal("delete")
                                .executes(ctx -> {
                                    ctx.getSource().getSender().sendMessage("menu delete");
                                    return Command.SINGLE_SUCCESS;
                                })))
                .build();
    }

    private @NotNull LiteralCommandNode<CommandSourceStack> listNode() {
        return literal("list")
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage("list help"); // TODO
                    return Command.SINGLE_SUCCESS;
                })
                .then(literal("activators")
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage("list activators"); // TODO
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(argument("group", StringArgumentType.word())
                                .executes(ctx -> {
                                    ctx.getSource().getSender().sendMessage("list activators " + StringArgumentType.getString(ctx, "group")); // TODO
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(literal("locations")
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage("list locations"); // TODO
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(argument("world", ArgumentTypes.world())
                                .executes(ctx -> {
                                    ctx.getSource().getSender().sendMessage("list locations " + ctx.getArgument("world", World.class).getName()); // TODO
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(literal("menus")
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage("list menus"); // TODO
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }

    private @NotNull LiteralCommandNode<CommandSourceStack> reloadNode() {
        return literal("reload")
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage("reload all");
                    return Command.SINGLE_SUCCESS;
                })
                .then(argument("options", StringArgumentType.word())
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage("reload specific " + StringArgumentType.getString(ctx, "options"));
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }

    private void help(@NotNull CommandContext<CommandSourceStack> ctx) {
        sendHelp(ctx,
                "create", "&7(&aactivator&7|&alocation&7|&amenu&7)&e <name>", "Create a new &enamed &aobject",
                "activator", "&a<name>", "Manage &anamed&r activator",
                "location", "&a<name>", "Manage &anamed&r location",
                "menu", "&a<name>", "Manage &anamed&r menu",
                "variable", "&a<name>", "Manage &anamed&r variable",
                "list", "&7(&aactivators&7|&alocations&7|&amenus&7)", "List &aobjects", // TODO List activities
                "reload", "", "Reload a plugin or its specific parts"
        );
    }
}