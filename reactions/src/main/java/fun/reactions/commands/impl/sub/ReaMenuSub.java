package fun.reactions.commands.impl.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.menu.InventoryMenu;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaMenuSub extends RaCommandBase {
    public ReaMenuSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("menu")
                .executes(ctx -> promptForName(ctx, "menu"))
                .then(argument("name", StringArgumentType.word())
                        .executes(this::nameHelp) // TODO Name suggestions
                        .then(literal("create")
                                .executes(ctx -> {
                                    createMenu(ctx, 3, null);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(argument("rows", IntegerArgumentType.integer(1, 6))
                                        .executes(ctx -> {
                                            createMenu(ctx, IntegerArgumentType.getInteger(ctx, "rows"), null);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(argument("title", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    createMenu(ctx,
                                                            IntegerArgumentType.getInteger(ctx, "rows"),
                                                            StringArgumentType.getString(ctx, "title"));
                                                    return Command.SINGLE_SUCCESS;
                                                }))))
                        .then(literal("open")
                                .executes(ctx -> { // TODO
                                    // open(ctx);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(literal("delete")
                                .executes(ctx -> { // TODO
                                    // delete(ctx);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .build();
    }

    private int nameHelp(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (!InventoryMenu.containsMenu(name)) {
            return suggestCreate(ctx, "menu " + name, "Menu", name, "create");
        }
        help(ctx, name);
        return Command.SINGLE_SUCCESS;
    }

    private void help(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String name) {
        sendHelp(ctx, "menu " + esc(name),
                "create", "&e[<rows>&6 [<title>]&e]", "Create menu with optional&e rows&r count and&e title",
                "open", "&e[<player>]", "Open a menu",
                "delete", "", "Delete a menu"
        );
    }

    private void createMenu(@NotNull CommandContext<CommandSourceStack> ctx, int rows, @Nullable String title) {
        String name = StringArgumentType.getString(ctx, "name");
        if (InventoryMenu.containsMenu(name)) {
            sendPrefixed(ctx, "Menu&c '" + esc(name) + "'&r already exists");
            return;
        }
        InventoryMenu.add(name, rows, title);
        sendPrefixed(ctx, "Menu&a '" + esc(name) + "'&r was created");
    }
}