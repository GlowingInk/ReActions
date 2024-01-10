package fun.reactions.commands.impl.sub;

import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
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
import static fun.reactions.commands.nodes.IntegerArgNode.Range.intRange;
import static fun.reactions.commands.nodes.IntegerArgNode.integerArg;
import static fun.reactions.commands.nodes.LiteralNode.literal;
import static fun.reactions.commands.nodes.StringArgNode.stringArg;

public final class ReaCreateSub extends RaCommandBase {
    public ReaCreateSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    @Override
    public @NotNull Node asNode() {
        return literal("create", this::help,
                new ActivatorSub(platform).asNode(),
                new LocationSub(platform).asNode(),
                new MenuSub(platform).asNode()
        );
    }

    private void help(@NotNull Parameters params, @NotNull CommandSender sender) {
        sendHelp(sender, params, "create",
                "activator", "&a<name> <type> &e[<parameters...>]", "Create &anamed&r activator with specified &atype&r and &eparameters",
                "location", "&a<name> &e[<world> <x> <y> <z> &6[<yaw> <pitch>]&e]", "Create &anamed&r location at your position, or with &especified coordinates",
                "menu", "&a<name> &e[<rows> <title>]", "Create &anamed&r menu with optional &erows&r count and &etitle"
        );
    }

    private static class ActivatorSub extends RaCommandBase {
        protected ActivatorSub(@NotNull ReActions.Platform platform) {
            super(platform);
        }

        @Override
        public @NotNull Node asNode() {
            return literal("activator",
                    stringArg("name", StringArgNode.Type.WORD,
                            stringArg("type", StringArgNode.Type.WORD, () -> platform.getActivatorTypes().getTypeNames(), this::activator,
                                    stringArg(
                                            "parameters",
                                            StringArgNode.Type.OPTIONAL_GREEDY,
                                            this::activator
                                    )
                            )
                    )
            );
        }

        private void activator(@NotNull Parameters params, @NotNull CommandSender sender) {
            ActivatorsManager activators = platform.getActivators();
            if (activators.getActivator(params.getString("name")) != null) {
                sendPrefixed(sender, "Activator &c'" + escape(params.getString("name")) + "'&r already exists");
                return;
            }

            ActivatorTypesRegistry types = platform.getActivatorTypes();
            ActivatorType type = ensure(
                    params.get("type", types::get),
                    "Activator type &c'" + escape(params.getString("type")) + "'&r doesn't exist"
            );

            Activator activator = ensure(type.createActivator(
                    new Logic(platform, type.getName(), params.getString("name")),
                    params.getParameters("parameters")
            ), "Failed to create activator&c!");

            activators.addActivator(activator, true);
            sendPrefixed(sender, "Activator &a'" + escape(activator.getLogic().getName()) + "'&r of type &a'" + escape(activator.getLogic().getType()) + "'&r was created");
        }
    }

    private static class LocationSub extends RaCommandBase {
        protected LocationSub(@NotNull ReActions.Platform platform) {
            super(platform);
        }

        @Override
        public @NotNull Node asNode() {
            return literal("location",
                    stringArg("name", StringArgNode.Type.WORD, this::pointLocation,
                            stringArg("world", StringArgNode.Type.WORD,
                                    doubleArg("x", doubleArg("y", doubleArg("z", this::location, doubleArg("yaw", doubleArg("pitch", this::location)))))
                            )
                    )
            );
        }

        private void pointLocation(@NotNull Parameters params, @NotNull CommandSender sender) {
            Location loc;
            if (sender instanceof Entity entity) {
                loc = entity.getLocation();
            } else if (sender instanceof BlockState block) {
                loc = block.getLocation();
            } else {
                sendPrefixed(sender, "You must be an entity or a command block to perform this command");
                return;
            }

            location(params.with(RealPosition.byLocation(loc)), sender);
        }

        private void location(@NotNull Parameters params, @NotNull CommandSender sender) {
            if (params.get("name", LocationHolder::getTpLoc) != null) {
                sendPrefixed(sender, "Location &c'" + escape(params.getString("name")) + "'&r already exists");
                return;
            }
            RealPosition pos = RealPosition.fromParameters(params);
            LocationHolder.addTpLoc(params.getString("name"), pos);
            sendPrefixed(sender, "Location &a'" + escape(params.getString("name")) + "'&r &7(" + pos + ")&r was created");
        }
    }

    private static class MenuSub extends RaCommandBase {
        protected MenuSub(@NotNull ReActions.Platform platform) {
            super(platform);
        }

        @Override
        public @NotNull Node asNode() {
            return literal("menu",
                    stringArg("name", StringArgNode.Type.WORD, this::menu,
                            integerArg("rows", intRange(1, 6),
                                    stringArg("title", StringArgNode.Type.GREEDY)
                            )
                    )
            );
        }

        private void menu(@NotNull Parameters params, @NotNull CommandSender sender) {
            if (InventoryMenu.containsMenu(params.getString("name"))) {
                sendPrefixed(sender, "Menu &c'" + escape(params.getString("name")) + "'&r already exists");
                return;
            }
            InventoryMenu.add(params.getString("name"), params.getInteger("rows", 3), params.getString("title"));
            sendPrefixed(sender, "Menu &a'" + escape(params.getString("name")) + "'&r was created");
        }
    }
}
