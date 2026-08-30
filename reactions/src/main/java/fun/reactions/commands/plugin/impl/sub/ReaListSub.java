package fun.reactions.commands.plugin.impl.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.plugin.RaCommandBase;
import fun.reactions.holders.LocationHolder;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.util.location.position.RealPosition;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaListSub extends RaCommandBase {
    public ReaListSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() { // TODO Nearby locs/activators
        return literal("list")
                .requires(permission("reactions.list"))
                .executes(this::help)
                .then(literal("activators")
                        .requires(permission("reactions.activator.view"))
                        .executes(ctx -> listActivators(ctx, null, 1))
                        .then(argument("group", StringArgumentType.word())
                                .suggests(suggestNames(() -> platform.getActivators().getGroupNames(), true))
                                .executes(ctx -> listActivators(ctx, filterArg(ctx, "group"), 1))
                                .then(argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listActivators(ctx, filterArg(ctx, "group"), IntegerArgumentType.getInteger(ctx, "page"))))))
                .then(literal("locations")
                        .requires(permission("reactions.location.view"))
                        .executes(ctx -> listLocations(ctx, null, 1))
                        .then(argument("world", StringArgumentType.word())
                                .suggests(suggestNames(() -> platform.getServer().getWorlds().stream().map(World::getName).toList(), true))
                                .executes(ctx -> listLocations(ctx, filterArg(ctx, "world"), 1))
                                .then(argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listLocations(ctx, filterArg(ctx, "world"), IntegerArgumentType.getInteger(ctx, "page"))))))
                .then(literal("menus")
                        .requires(permission("reactions.menu.view"))
                        .executes(ctx -> listMenus(ctx, 1))
                        .then(argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> listMenus(ctx, IntegerArgumentType.getInteger(ctx, "page")))))
                // TODO Timers
                .build();
    }

    private int help(@NotNull CommandContext<CommandSourceStack> ctx) {
        return sendHelp(ctx, "list",
                "activators", "&a[<group>|_] &e[<page>]", "List all activators, optionally filtered by&a group",
                "locations", "&a[<world>|_] &e[<page>]", "List all locations, optionally filtered by&a world",
                "menus", "&6[<page>]", "List all menus"
        );
    }

    private int listActivators(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable String group, int page) {
        Collection<Activator> found = group != null
                ? platform.getActivators().search().byGroup(group)
                : platform.getActivators().search().all();
        List<String> lines = new ArrayList<>();
        for (Activator activator : found) {
            Logic logic = activator.getLogic();
            String display = "&7" + esc(logic.getGroup()) + "/&6" + esc(logic.getName()) + "&e (" + esc(logic.getType()) + ")";
            lines.add(listLine(ctx, display, "activator " + logic.getName()));
        }
        return sendPage(ctx, "list activators " + esc(group == null ? "*" : group), "Activators", lines, page);
    }

    private int listLocations(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable String world, int page) {
        List<String> lines = new ArrayList<>();
        for (String name : LocationHolder.getTpLocNames()) {
            RealPosition pos = LocationHolder.getTpPosition(name);
            if (pos == null || (world != null && !pos.worldName().equalsIgnoreCase(world))) continue;
            String display = "&6" + esc(name) + "&7 (" + esc(pos.toString()) + ")";
            lines.add(listLine(ctx, display, "location " + name));
        }
        return sendPage(ctx, "list locations " + esc(world == null ? "*" : world), "Locations", lines, page);
    }

    private int listMenus(@NotNull CommandContext<CommandSourceStack> ctx, int page) {
        List<String> lines = new ArrayList<>();
        for (String name : InventoryMenu.getMenuNames()) {
            lines.add(listLine(ctx, "&6" + esc(name), "menu " + name));
        }
        return sendPage(ctx, "list menus", "Menus", lines, page);
    }

    private static @Nullable String filterArg(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String arg) {
        String value = StringArgumentType.getString(ctx, arg);
        return value.equals("*") ? null : value;
    }

    private static @NotNull String listLine(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String display, @NotNull String command) {
        if (!(ctx.getSource().getSender() instanceof Player)) return display;
        String start = "/" + rootLabel(ctx) + " " + command + " ";
        return "&[" + display + "](click:suggest " + start + ")(hover:text &7" + start.trim() + ")";
    }
}
