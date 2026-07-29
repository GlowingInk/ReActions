package fun.reactions.commands.impl.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.holders.LocationHolder;
import fun.reactions.util.location.position.RealPosition;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.math.FinePosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaLocationSub extends RaCommandBase {
    public ReaLocationSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("location")
                .requires(permission("reactions.location"))
                .executes(ctx -> promptForName(ctx, "location"))
                .then(argument("name", StringArgumentType.word())
                        .suggests((_, builder) -> {
                            String remaining = builder.getRemaining();
                            LocationHolder.getTpLocNames().stream()
                                    .filter(s -> s.startsWith(remaining))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(this::nameHelp)
                        .then(literal("create")
                                .requires(permission("reactions.location.edit"))
                                .executes(ctx -> {
                                    createLocation(ctx);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(argument("world", ArgumentTypes.world())
                                        .then(argument("position", ArgumentTypes.finePosition(false))
                                                .executes(ctx -> {
                                                    createLocation(ctx, false);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                                .then(argument("yaw", DoubleArgumentType.doubleArg())
                                                        .then(argument("pitch", DoubleArgumentType.doubleArg())
                                                                .executes(ctx -> {
                                                                    createLocation(ctx, true);
                                                                    return Command.SINGLE_SUCCESS;
                                                                }))))))
                        .then(literal("info")
                                .requires(permission("reactions.location.view"))
                                .executes(ctx -> {
                                    info(ctx);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(literal("delete")
                                .requires(permission("reactions.location.edit"))
                                .executes(ctx -> {
                                    delete(ctx);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(literal("tp")
                                .requires(permission("reactions.location.use"))
                                .executes(ctx -> {
                                    teleport(ctx, null);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(argument("player", ArgumentTypes.player())
                                        .executes(ctx -> {
                                            teleport(ctx, ctx.getArgument("player", PlayerSelectorArgumentResolver.class));
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(literal("move")
                                .requires(permission("reactions.location.edit"))
                                .executes(ctx -> {
                                    move(ctx, null);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(argument("position", ArgumentTypes.finePosition(false))
                                        .executes(ctx -> {
                                            move(ctx, ctx.getArgument("position", FinePositionResolver.class));
                                            return Command.SINGLE_SUCCESS;
                                        })))).build();
    }

    private int nameHelp(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (LocationHolder.getTpLoc(name) == null) {
            return suggestCreate(ctx, "location " + name, "Location", name, "create");
        }
        help(ctx, name);
        return Command.SINGLE_SUCCESS;
    }

    private void help(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String name) {
        sendHelp(ctx, "location " + esc(name),
                "create", "&e[<world> <x> <y> <z>&6 [<yaw> <pitch>]&e]", "Create location here or at&e specified coordinates",
                "info", "", "Get info about a location",
                "delete", "", "Delete a location",
                "tp", "[player]", "Teleport yourself or a &especified player",
                "move", "[position]", "Move location to yourself or onto &especified position"
        );
    }

    private void createLocation(@NotNull CommandContext<CommandSourceStack> ctx) {
        Entity executor = ctx.getSource().getExecutor();
        if (executor == null) {
            sendPrefixed(ctx, "You must be an entity or a command block to perform this command");
            return;
        }
        createLocation(ctx, RealPosition.byLocation(executor.getLocation()));
    }

    private void createLocation(@NotNull CommandContext<CommandSourceStack> ctx, boolean withRotation) throws CommandSyntaxException {
        World world = ctx.getArgument("world", World.class);
        FinePosition pos = ctx.getArgument("position", FinePositionResolver.class).resolve(ctx.getSource());
        Location loc = new Location(
                world, pos.x(), pos.y(), pos.z(),
                withRotation ? (float) DoubleArgumentType.getDouble(ctx, "yaw") : 0f,
                withRotation ? (float) DoubleArgumentType.getDouble(ctx, "pitch") : 0f
        );
        createLocation(ctx, RealPosition.byLocation(loc));
    }

    private void createLocation(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull RealPosition pos) {
        String name = StringArgumentType.getString(ctx, "name");
        if (LocationHolder.getTpLoc(name) != null) {
            sendPrefixed(ctx, "Location&c '" + esc(name) + "'&r already exists");
            return;
        }
        LocationHolder.addTpLoc(name, pos);
        LocationHolder.saveLocs();
        sendPrefixed(ctx, "Location&a '&{name}'&r&7 (&{pos})&r was created", Map.of(
                "name", name,
                "pos", pos.toString()
        ));
    }

    private void info(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        RealPosition loc = getLocation(ctx);
        if (loc == null) {
            return;
        }
        CommandSender sender = ctx.getSource().getSender();
        sender.sendMessage("");
        sender.sendMessage(inky("&6&l" + esc(name)));
        sendInky(sender, "  &7World&r: &f" + loc.worldName());
        sendInky(sender, "  &7Position&r: &f" + loc.x() + " " + loc.y() + " " + loc.z());
        sendInky(sender, "  &7Head&r: &f" + loc.yaw() + " " + loc.pitch());
        sendInky(sender, "&[  &eClick here to copy location](click:copy " + esc(loc.toString()) + ")(hover:text &7" + esc(loc.toString()) + ")");
    }

    private void delete(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (getLocation(ctx) == null) {
            return;
        }
        LocationHolder.removeTpLoc(name);
        sendPrefixed(ctx, "Location &a'" + esc(name) + "'&r was deleted.");
    }

    private void teleport(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable PlayerSelectorArgumentResolver resolver) throws CommandSyntaxException {
        RealPosition loc = getLocation(ctx);
        if (loc == null) {
            return;
        }
        CommandSender sender = ctx.getSource().getSender();
        Player player = resolver != null
                ? resolver.resolve(ctx.getSource()).getFirst()
                : (sender instanceof Player pl ? pl : null);
        if (player != null) {
            player.teleport(loc.toLocation(platform.getServer()));
            sendPrefixed(sender, "Teleported to location &a'&{name}'&r.", Map.of("name", StringArgumentType.getString(ctx, "name")));
        } else {
            sendPrefixed(sender, "Couldn't find selected player");
        }
    }

    private void move(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable FinePositionResolver resolver) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        RealPosition pos;
        if (resolver != null) {
            Entity executor = ctx.getSource().getExecutor();
            if (executor == null) {
                sendPrefixed(ctx, "Only a player or entity can &especify a position&r, since a &aworld&r can't be inferred otherwise.");
                return;
            }
            FinePosition finePos = resolver.resolve(ctx.getSource());
            pos = RealPosition.byLocation(new Location(executor.getWorld(), finePos.x(), finePos.y(), finePos.z()));
        } else {
            if (!(ctx.getSource().getExecutor() instanceof Entity entity)) {
                sendPrefixed(ctx, "You must &especify a position&r when running this as console.");
                return;
            }
            pos = RealPosition.byLocation(entity.getLocation());
        }
        LocationHolder.addTpLoc(name, pos);
        sendPrefixed(ctx, "Location &a'" + esc(name) + "'&r was moved.");
    }

    private @Nullable RealPosition getLocation(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        var tpPos =  LocationHolder.getTpPosition(name);
        if (tpPos == null) {
            sendInky(ctx, "Location &c'" + esc(name) + "'&r doesn't exist!");
        }
        return tpPos;
    }
}