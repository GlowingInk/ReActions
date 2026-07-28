package fun.reactions.commands.impl.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.environment.Variables;
import fun.reactions.util.parameter.Parameters;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
                        .suggests((_, builder) -> {
                            String remaining = builder.getRemaining();
                            InventoryMenu.getMenuNames().stream()
                                    .filter(s -> s.startsWith(remaining))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(this::nameHelp)
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
                                                })))
                                .then(literal("chest")
                                        .executes(ctx -> createFromChest(ctx, null))
                                        .then(argument("title", StringArgumentType.greedyString())
                                                .executes(ctx -> createFromChest(ctx, StringArgumentType.getString(ctx, "title"))))))
                        .then(literal("open")
                                .executes(ctx -> open(ctx, null))
                                .then(argument("player", ArgumentTypes.player())
                                        .executes(ctx -> open(ctx, ctx.getArgument("player", PlayerSelectorArgumentResolver.class)))))
                        .then(literal("delete")
                                .executes(this::delete)))
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
                "create chest", "&e[<title>]", "Create menu from your currently open&e container",
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

    private int createFromChest(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable String title) {
        String name = StringArgumentType.getString(ctx, "name");
        if (InventoryMenu.containsMenu(name)) {
            sendPrefixed(ctx, "Menu&c '" + esc(name) + "'&r already exists");
            return Command.SINGLE_SUCCESS;
        }
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            sendPrefixed(ctx, "Only a player can create a menu from a chest.");
            return Command.SINGLE_SUCCESS;
        }
        Block target = player.getTargetBlockExact(6);
        if (target == null || !(target.getState() instanceof Container container)) {
            sendPrefixed(ctx, "You must be &alooking at&r a chest (or other container) to do this.");
            return Command.SINGLE_SUCCESS;
        }
        InventoryMenu.addFromInventory(name, container.getInventory(), title);
        sendPrefixed(ctx, "Menu&a '" + esc(name) + "'&r was created from the targeted chest");
        return Command.SINGLE_SUCCESS;
    }

    private int open(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable PlayerSelectorArgumentResolver resolver) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        if (!checkMenuExists(ctx, name)) return Command.SINGLE_SUCCESS;

        CommandSender sender = ctx.getSource().getSender();
        Player player = resolver != null
                ? resolver.resolve(ctx.getSource()).getFirst()
                : (sender instanceof Player pl ? pl : null);
        if (player == null) {
            sendPrefixed(sender, "Couldn't find selected player");
            return Command.SINGLE_SUCCESS;
        }
        InventoryMenu.createAndOpenInventory(player, Parameters.fromMap(Map.of("menu", name)), new Variables());
        sendPrefixed(sender, "Menu &a'&{name}'&r was opened for &a&{player}&r.", Map.of(
                "name", name,
                "player", player.getName()
        ));
        return Command.SINGLE_SUCCESS;
    }

    private int delete(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (!checkMenuExists(ctx, name)) return Command.SINGLE_SUCCESS;

        InventoryMenu.remove(name);
        sendPrefixed(ctx, "Menu &a'" + esc(name) + "'&r was deleted.");
        return Command.SINGLE_SUCCESS;
    }

    private boolean checkMenuExists(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String name) {
        if (InventoryMenu.containsMenu(name)) return true;
        sendInky(ctx, "Menu &c'" + esc(name) + "'&r doesn't exist!");
        return false;
    }
}