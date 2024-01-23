package fun.reactions.commands.impl.sub;

import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.commands.nodes.Node;
import fun.reactions.commands.nodes.StringArgNode;
import fun.reactions.holders.LocationHolder;
import fun.reactions.util.location.position.RealPosition;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static fun.reactions.commands.nodes.LiteralNode.literal;
import static fun.reactions.commands.nodes.StringArgNode.stringArg;

public class ReaLocationSub extends RaCommandBase {
    public ReaLocationSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    @Override
    public @NotNull Node asNode() {
        return literal("location", stringArg("name", StringArgNode.Type.WORD, this::help,
                        literal("info", this::info),
                        literal("delete", this::delete),
                        literal("tp", this::teleport, stringArg("player", StringArgNode.Type.OPTIONAL_GREEDY)),
                        literal("move", this::move, stringArg("location", StringArgNode.Type.OPTIONAL_GREEDY))
                )
        );
    }

    private void help(@NotNull Parameters params, @NotNull CommandSender sender) {
        getLocation(params);
        sendHelp(sender, params, "activator " + escape(params.getString("name")),
                "info", "", "Get info about a location",
                "delete", "", "Delete a location",
                "tp", "[player]", "Teleport yourself or a &especified player",
                "move", "[location]", "Move location to yourself or onto &especified location"
        );
    }

    private void info(@NotNull Parameters params, @NotNull CommandSender sender) {
        RealPosition loc = getLocation(params);
        sendInky(sender, "Location '" + escape(params.getString("name")) + "':\n" +
                "  World: " + loc.worldName() + "\n" +
                "  Coordinates: " + loc.x() + ", " + loc.y() + ", " + loc.z() + "\n" +
                "  Head: " + loc.yaw() + ", " + loc.pitch());
    }

    private void delete(@NotNull Parameters params, @NotNull CommandSender sender) {
        getLocation(params);
        LocationHolder.removeTpLoc(params.getString("name"));
        sender.sendMessage("Deleted");// TODO Removal message
    }

    private void teleport(@NotNull Parameters params, @NotNull CommandSender sender) {
        RealPosition loc = getLocation(params);
        Server server = platform.getServer();
        Player player = params.get("player", (str) -> {
            if (str == null) {
                if (sender instanceof Player pl) return pl;
            } else {
                Player pl = server.getPlayerExact(str);
                if (pl != null) {
                    return pl;
                }
                try {
                    return server.getPlayer(UUID.fromString(str));
                } catch (Exception ignored) {}
            }
            return null;
        });
        if (player != null) {
            player.teleport(loc.toLocation(server));
            // TODO Message
        } else {
            sender.sendMessage("Couldn't find selected player"); // TODO Better message
        }
    }

    private void move(@NotNull Parameters params, @NotNull CommandSender sender) {
        getLocation(params);
        String name = params.getString("name");
        RealPosition pos = params.getOr("location", RealPosition::byString, () -> {
            if (sender instanceof Entity entity) {
                return RealPosition.byLocation(entity.getLocation());
            }
            return null;
        });
        if (pos != null) {
            LocationHolder.addTpLoc(name, pos);
            // TODO Message
        } else {
            sender.sendMessage("Console not allowed"); // TODO Better message
        }
    }

    private @NotNull RealPosition getLocation(Parameters params) {
        String name = params.getString("name");
        return ensure(LocationHolder.getTpPosition(name), "Location &c'" + escape(name) + "'&r doesn't exist!");
    }
}
