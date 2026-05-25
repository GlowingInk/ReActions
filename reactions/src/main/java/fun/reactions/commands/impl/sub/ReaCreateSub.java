package fun.reactions.commands.impl.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.holders.LocationHolder;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activators.type.ActivatorTypesRegistry;
import fun.reactions.util.location.position.RealPosition;
import fun.reactions.util.parameter.Parameters;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.math.FinePosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaCreateSub extends RaCommandBase {
    public ReaCreateSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("create")
                .executes(ctx -> {
                    help(ctx);
                    return Command.SINGLE_SUCCESS;
                })
                .then(new ActivatorSub(platform).asNode())
                .then(new LocationSub(platform).asNode())
                .then(new MenuSub(platform).asNode())
                .then(new VariableSub(platform).asNode())
                .build();
    }

    private void help(@NotNull CommandContext<CommandSourceStack> ctx) {
        sendHelp(ctx, "create",
                "activator", "&a<name> <type>&e [<parameters...>]", "Create&a named&r activator with specified&a type&r and&e parameters",
                "location", "&a<name>&e [<world> <x> <y> <z>&6 [<yaw> <pitch>]&e]", "Create&a named&r location at your position, or with&e specified coordinates",
                "menu", "&a<name>&e [<rows> <title>]", "Create&a named&r menu with optional&e rows&r count and&e title",
                "variable", "&a<name>&e [value]", "Create&a named&r variable with optional&e value"
        );
    }

    private static class VariableSub extends RaCommandBase {
        protected VariableSub(@NotNull ReActions.Platform platform) {
            super(platform);
        }

        public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
            return literal("variable")
                    .then(argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                variable(ctx, "");
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(argument("value", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        variable(ctx, StringArgumentType.getString(ctx, "value"));
                                        return Command.SINGLE_SUCCESS;
                                    })))
                    .build();
        }

        private void variable(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String value) {
            String name = StringArgumentType.getString(ctx, "name");
            String[] varName = name.split(":");
            platform.getPersistentVariables().setVariable(
                    varName.length > 1 ? varName[0] : null,
                    varName.length > 1 ? varName[1] : varName[0],
                    value
            );
            sendPrefixed(ctx.getSource().getSender(),
                    "Variable&a '" + escape(name) + "'&r was created with the value:\n" + escape(value));
        }
    }

    private static class ActivatorSub extends RaCommandBase {
        protected ActivatorSub(@NotNull ReActions.Platform platform) {
            super(platform);
        }

        public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
            return literal("activator")
                    .then(argument("name", StringArgumentType.word())
                            .then(argument("type", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        platform.getActivatorTypes().getTypeNames().stream()
                                                .filter(s -> s.startsWith(builder.getRemaining()))
                                                .forEach(builder::suggest);
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> {
                                        activator(ctx, "");
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(argument("parameters", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                activator(ctx, StringArgumentType.getString(ctx, "parameters"));
                                                return Command.SINGLE_SUCCESS;
                                            }))))
                    .build();
        }

        private void activator(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String rawParameters) {
            CommandSender sender = ctx.getSource().getSender();
            String name = StringArgumentType.getString(ctx, "name");
            String typeName = StringArgumentType.getString(ctx, "type");
            ActivatorsManager activators = platform.getActivators();
            if (activators.getActivator(name) != null) {
                sendPrefixed(sender, "Activator&c '" + escape(name) + "'&r already exists");
                return;
            }
            ActivatorTypesRegistry types = platform.getActivatorTypes();
            ActivatorType type = ensure(types.get(typeName),
                    "Activator type&c '" + escape(typeName) + "'&r doesn't exist");
            Activator activator = ensure(
                    type.createActivator(
                            new Logic(platform, type.getName(), name),
                            Parameters.fromString(rawParameters)
                    ),
                    "Failed to create activator&c!"
            );
            activators.addActivator(activator, true);
            sendPrefixed(sender, "Activator&a '" + escape(activator.getLogic().getName())
                    + "'&r of type&a '" + escape(activator.getLogic().getType()) + "'&r was created");
        }
    }

    private static class LocationSub extends RaCommandBase {
        protected LocationSub(@NotNull ReActions.Platform platform) {
            super(platform);
        }

        public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
            return literal("location")
                    .then(argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                pointLocation(ctx);
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(argument("world", ArgumentTypes.world())
                                    .then(argument("position", ArgumentTypes.finePosition(false))
                                            .executes(ctx -> {
                                                location(ctx, false);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(argument("yaw", DoubleArgumentType.doubleArg())
                                                    .then(argument("pitch", DoubleArgumentType.doubleArg())
                                                            .executes(ctx -> {
                                                                location(ctx, true);
                                                                return Command.SINGLE_SUCCESS;
                                                            }))))))
                    .build();
        }

        private void pointLocation(@NotNull CommandContext<CommandSourceStack> ctx) {
            CommandSender sender = ctx.getSource().getSender();
            Location loc;
            if (sender instanceof Entity entity) {
                loc = entity.getLocation();
            } else if (sender instanceof BlockState block) {
                loc = block.getLocation();
            } else {
                sendPrefixed(sender, "You must be an entity or a command block to perform this command");
                return;
            }
            createLocation(ctx, RealPosition.byLocation(loc));
        }

        private void location(@NotNull CommandContext<CommandSourceStack> ctx, boolean withRotation) throws CommandSyntaxException {
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
            CommandSender sender = ctx.getSource().getSender();
            String name = StringArgumentType.getString(ctx, "name");
            if (LocationHolder.getTpLoc(name) != null) {
                sendPrefixed(sender, "Location&c '" + escape(name) + "'&r already exists");
                return;
            }
            LocationHolder.addTpLoc(name, pos);
            LocationHolder.saveLocs();
            sendPrefixed(sender, "Location&a '" + escape(name) + "'&r&7 (" + pos + ")&r was created");
        }
    }

    private static class MenuSub extends RaCommandBase {
        protected MenuSub(@NotNull ReActions.Platform platform) {
            super(platform);
        }

        public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
            return literal("menu")
                    .then(argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                menu(ctx, 3, null);
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(argument("rows", IntegerArgumentType.integer(1, 6))
                                    .executes(ctx -> {
                                        menu(ctx, IntegerArgumentType.getInteger(ctx, "rows"), null);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(argument("title", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                menu(ctx,
                                                        IntegerArgumentType.getInteger(ctx, "rows"),
                                                        StringArgumentType.getString(ctx, "title"));
                                                return Command.SINGLE_SUCCESS;
                                            }))))
                    .build();
        }

        private void menu(@NotNull CommandContext<CommandSourceStack> ctx, int rows, @Nullable String title) {
            CommandSender sender = ctx.getSource().getSender();
            String name = StringArgumentType.getString(ctx, "name");
            if (InventoryMenu.containsMenu(name)) {
                sendPrefixed(sender, "Menu&c '" + escape(name) + "'&r already exists");
                return;
            }
            InventoryMenu.add(name, rows, title);
            sendPrefixed(sender, "Menu&a '" + escape(name) + "'&r was created");
        }
    }
}