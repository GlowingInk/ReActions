package fun.reactions.commands.impl;

import fun.reactions.ReActions;
import fun.reactions.commands.RaCommand;
import fun.reactions.commands.nodes.Node;
import fun.reactions.commands.nodes.StringArgNode;
import fun.reactions.holders.LocationHolder;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activators.type.ActivatorTypesRegistry;
import fun.reactions.util.location.position.RealPosition;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import static fun.reactions.commands.nodes.DoubleArgNode.doubleArg;
import static fun.reactions.commands.nodes.IntegerArgNode.integerArg;
import static fun.reactions.commands.nodes.LiteralNode.literal;
import static fun.reactions.commands.nodes.StringArgNode.stringArg;

public final class CreateSub extends RaCommand {
    public CreateSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    @Override
    public @NotNull Node asNode() {
        return literal("create", this::help,
                new ActivatorSub(platform).asNode(),
                literal("location", this::pointLocation,
                        stringArg("world", StringArgNode.Type.WORD,
                                doubleArg("x", doubleArg("y", doubleArg("z", this::location, doubleArg("yaw", doubleArg("pitch", this::location)))))
                        )
                ),
                literal("menu",
                        integerArg("rows", 1, 6, this::menu,
                                stringArg("title", StringArgNode.Type.GREEDY, this::menu)
                        )
                )
        );
    }

    private void help(@NotNull Parameters params, @NotNull CommandSender sender) {
        sender.sendMessage("Some help thingy");
    }

    private static class ActivatorSub extends RaCommand {
        protected ActivatorSub(@NotNull ReActions.Platform platform) {
            super(platform);
        }

        @Override
        public @NotNull Node asNode() {
            return literal("activator", this::help,
                    stringArg("type", StringArgNode.Type.WORD, () -> platform.getActivatorTypes().getTypeNames(),
                            stringArg("name", StringArgNode.Type.WORD,
                                    stringArg(
                                            "parameters",
                                            StringArgNode.Type.OPTIONAL_GREEDY,
                                            this::activator
                                    )
                            )
                    )
            );
        }

        private void help(@NotNull Parameters params, @NotNull CommandSender sender) {
            // TODO
            sender.sendMessage(params.getString(Node.COMMAND_KEY));
        }

        private void activator(@NotNull Parameters params, @NotNull CommandSender sender) {
            ActivatorsManager activators = platform.getActivators();
            if (activators.getActivator(params.getString("name")) != null) {
                sender.sendMessage("Activator " + params.getString("name") + " already exists");
                return;
            }
            ActivatorTypesRegistry types = platform.getActivatorTypes();
            ActivatorType type = params.get("type", types::get);
            if (type == null) {
                sender.sendMessage("Activator type '" + params.getString("type") + "' doesn't exist");
                return;
            }
            Activator activator = type.createActivator(
                    new Logic(platform, type.getName(), params.getString("name")),
                    params.getParameters("parameters")
            );
            if (activator == null) {
                sender.sendMessage("Failed to create activator");
                return;
            }
            activators.addActivator(activator, true);
            sender.sendMessage("Activator " + activator.getLogic().getName() + " of type " + activator.getLogic().getType() + " was created");
        }
    }

    private void pointLocation(@NotNull Parameters params, @NotNull CommandSender sender) {
        Location loc;
        if (sender instanceof Entity entity) {
            loc = entity.getLocation();
        } else if (sender instanceof BlockState block) {
            loc = block.getLocation();
        } else {
            sender.sendMessage("You must be an entity or a command block to perform this command");
            return;
        }

        location(params.with(RealPosition.byLocation(loc)), sender);
    }

    private void location(@NotNull Parameters params, @NotNull CommandSender sender) {
        if (params.get("name", LocationHolder::getTpLoc) != null) {
            sender.sendMessage("Location " + params.getString("name") + " already exists");
            return;
        }
        RealPosition pos = RealPosition.fromParameters(params);
        LocationHolder.addTpLoc(params.getString("name"), pos);
        sender.sendMessage("Location " + params.getString("name") + " (" + pos + ") was created");
    }

    private void menu(@NotNull Parameters params, @NotNull CommandSender sender) {
        if (InventoryMenu.containsMenu(params.getString("name"))) {
            sender.sendMessage("Menu " + params.getString("name") + " already exists");
            return;
        }
        InventoryMenu.add(params.getString("menu"), params.getInteger("rows"), params.getString("title"));
        sender.sendMessage("Menu " + params.getString("name") + " was created");
    }
}
