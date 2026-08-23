package fun.reactions.commands.plugin.impl.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.plugin.RaCommandBase;
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

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaMenuSub extends RaCommandBase {
    public ReaMenuSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("menu")
                .requires(permission("reactions.menu"))
                .executes(ctx -> promptForName(ctx, "menu"))
                .then(argument("name", StringArgumentType.word())
                        .suggests(suggestNames(InventoryMenu::getMenuNames, false))
                        .executes(this::help)
                        .then(literal("create")
                                .requires(permission("reactions.menu.edit"))
                                .executes(ctx -> {
                                    create(ctx, 3, null);
                                    return SINGLE_SUCCESS;
                                })
                                .then(argument("rows", IntegerArgumentType.integer(1, 6))
                                        .executes(ctx -> {
                                            create(ctx, IntegerArgumentType.getInteger(ctx, "rows"), null);
                                            return SINGLE_SUCCESS;
                                        })
                                        .then(argument("title", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    create(
                                                            ctx,
                                                            IntegerArgumentType.getInteger(ctx, "rows"),
                                                            StringArgumentType.getString(ctx, "title")
                                                    );
                                                    return SINGLE_SUCCESS;
                                                })))
                                .then(literal("chest")
                                        .executes(ctx -> createFromChest(ctx, null))
                                        .then(argument("title", StringArgumentType.greedyString())
                                                .executes(ctx -> createFromChest(ctx, StringArgumentType.getString(ctx, "title"))))))
                        .then(literal("open")
                                .requires(permission("reactions.menu.use"))
                                .executes(ctx -> open(ctx, null))
                                .then(argument("player", ArgumentTypes.player())
                                        .executes(ctx -> open(ctx, ctx.getArgument("player", PlayerSelectorArgumentResolver.class)))))
                        .then(literal("delete")
                                .requires(permission("reactions.menu.edit"))
                                .executes(this::delete)))
                .build();
    }

    private int help(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        return sendHelp(ctx, "menu", name, InventoryMenu.containsMenu(name), "create",
                "create", "&e[<rows>&6 [<title>]&e]", "Create menu with optional&e rows&r count and&e title",
                "create chest", "&e[<title>]", "Create menu from your currently targeted&e chest",
                "open", "&e[<player>]", "Open a menu",
                "delete", "", "Delete a menu"
        );
    }

    private void create(@NotNull CommandContext<CommandSourceStack> ctx, int rows, @Nullable String title) {
        String name = StringArgumentType.getString(ctx, "name");
        if (InventoryMenu.containsMenu(name)) {
            sendAlreadyExists(ctx, "Menu", name);
            return;
        }
        InventoryMenu.add(name, rows, title);
        sendPrefixed(ctx, "Menu&a '" + esc(name) + "'&r was created");
    }

    private int createFromChest(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable String title) {
        String name = StringArgumentType.getString(ctx, "name");
        if (InventoryMenu.containsMenu(name)) {
            sendAlreadyExists(ctx, "Menu", name);
            return SINGLE_SUCCESS;
        }
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            sendPrefixed(ctx, "Only a player can create a menu from a chest.");
            return SINGLE_SUCCESS;
        }
        Block target = player.getTargetBlockExact(6);
        if (target == null || !(target.getState() instanceof Container container)) {
            sendPrefixed(ctx, "You must be&a looking at&r a chest to do this.");
            return SINGLE_SUCCESS;
        }
        InventoryMenu.addFromInventory(name, container.getInventory(), title);
        sendPrefixed(ctx, "Menu&a '" + esc(name) + "'&r was created from the targeted chest");
        return SINGLE_SUCCESS;
    }

    private int open(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable PlayerSelectorArgumentResolver resolver) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        if (!menuExists(ctx, name)) return SINGLE_SUCCESS;

        CommandSender sender = ctx.getSource().getSender();
        Player player = resolver != null
                ? resolver.resolve(ctx.getSource()).getFirst()
                : (sender instanceof Player pl ? pl : null);
        if (player == null) {
            sendPrefixed(sender, "Couldn't find selected player");
            return SINGLE_SUCCESS;
        }
        InventoryMenu.createAndOpenInventory(player, Parameters.fromMap(Map.of("menu", name)), new Variables());
        sendPrefixed(sender, "Menu &a'&{name}'&r was opened for &a&{player}&r.", Map.of(
                "name", name,
                "player", player.getName()
        ));
        return SINGLE_SUCCESS;
    }

    private int delete(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (!menuExists(ctx, name)) return SINGLE_SUCCESS;

        InventoryMenu.remove(name);
        sendPrefixed(ctx, "Menu &a'" + esc(name) + "'&r was deleted.");
        return SINGLE_SUCCESS;
    }

    private boolean menuExists(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String name) {
        if (InventoryMenu.containsMenu(name)) return true;
        sendNotFound(ctx, "Menu", name);
        return false;
    }
}