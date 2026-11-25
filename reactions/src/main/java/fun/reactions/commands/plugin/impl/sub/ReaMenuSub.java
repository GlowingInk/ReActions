package fun.reactions.commands.plugin.impl.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.plugin.RaCommandBase;
import fun.reactions.menu.Menu;
import fun.reactions.menu.MenuManager;
import fun.reactions.model.environment.Variables;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaMenuSub extends RaCommandBase {
    private final MenuManager menus;

    public ReaMenuSub(@NotNull ReActions.Platform platform) {
        super(platform);
        this.menus = platform.getMenus();
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("menu")
                .requires(permission("reactions.menu"))
                .executes(ctx -> promptForName(ctx, "menu"))
                .then(argument("name", StringArgumentType.word())
                        .suggests(suggestNames(menus::getNames, false))
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
        return sendHelp(ctx, "menu", name, menus.contains(name), "create",
                "create", "&a[<rows>&e [<title>]&a]", "Create menu with optional&e rows&r count and&e title",
                "create chest", "&a[<title>]", "Create menu from your currently targeted&e chest",
                "open", "&a[<player>]", "Open a menu",
                "delete", "", "Delete a menu"
        );
    }

    private void create(@NotNull CommandContext<CommandSourceStack> ctx, int rows, @Nullable String title) {
        String name = StringArgumentType.getString(ctx, "name");
        if (!menus.add(name, Menu.empty(rows, title))) {
            sendAlreadyExists(ctx, "Menu", name);
            return;
        }
        sendPrefixed(ctx, "Menu&a '" + esc(name) + "'&r was created.");
    }

    private int createFromChest(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable String title) {
        String name = StringArgumentType.getString(ctx, "name");
        if (menus.contains(name)) {
            sendAlreadyExists(ctx, "Menu", name);
            return SINGLE_SUCCESS;
        }
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            sendPrefixed(ctx, "Only a player can create a menu from a chest.");
            return SINGLE_SUCCESS;
        }
        Block target = player.getTargetBlockExact(6);
        if (target == null || !(target.getState() instanceof Chest chest)) {
            sendPrefixed(ctx, "You must be&a looking at&r a chest to do this.");
            return SINGLE_SUCCESS;
        }
        menus.add(name, Menu.fromInventory(chest.getInventory(), title, platform.getItems()));
        sendPrefixed(ctx, "Menu&a '" + esc(name) + "'&r was created from the targeted chest.");
        return SINGLE_SUCCESS;
    }

    private int open(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable PlayerSelectorArgumentResolver resolver) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        Menu menu = menus.getMenu(name);
        if (menu == null) {
            sendNotFound(ctx, "Menu", name);
            return SINGLE_SUCCESS;
        }

        CommandSender sender = ctx.getSource().getSender();
        Player player = resolver != null
                ? resolver.resolve(ctx.getSource()).getFirst()
                : (sender instanceof Player pl ? pl : null);
        if (player == null) {
            sendPrefixed(sender, "Couldn't find selected player.");
            return SINGLE_SUCCESS;
        }
        menus.open(player, menu, new Variables());
        sendPrefixed(sender, "Menu &a'&{name}'&r was opened for &a&{player}&r.", Map.of(
                "name", name,
                "player", player.getName()
        ));
        return SINGLE_SUCCESS;
    }

    private int delete(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (!menus.remove(name)) {
            sendNotFound(ctx, "Menu", name);
            return SINGLE_SUCCESS;
        }
        sendPrefixed(ctx, "Menu &a'" + esc(name) + "'&r was deleted.");
        return SINGLE_SUCCESS;
    }
}