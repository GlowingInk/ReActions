package fun.reactions.commands.impl;

import fun.reactions.ReActionsPlugin;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.commands.nodes.Node;
import fun.reactions.commands.nodes.StringArgNode;
import fun.reactions.util.parameter.Parameters;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static fun.reactions.commands.nodes.CommandNode.command;
import static fun.reactions.commands.nodes.LiteralNode.literal;
import static fun.reactions.commands.nodes.StringArgNode.stringArg;

public class ReactionsCommand extends RaCommandBase {
    private final Commodore commodore;
    private final PluginCommand reactionsCommand;

    public ReactionsCommand(@NotNull ReActionsPlugin plugin) {
        super(plugin);
        this.commodore = CommodoreProvider.getCommodore(plugin);
        this.reactionsCommand = Objects.requireNonNull(plugin.getCommand("reactions"));
    }

    @Override
    public @NotNull Node asNode() {
        return command(commodore, reactionsCommand, this::help,
                    new ReaCreateSub(platform).asNode(),
                    new ReaActivatorSub(platform).asNode(),
                    literal("location", (p, s) -> s.sendMessage("loc help"),
                            stringArg("name", StringArgNode.Type.WORD, (p, s) -> s.sendMessage("loc specific help"),
                                    literal("info", (p, s) -> s.sendMessage("loc info")),
                                    literal("delete", (p, s) -> s.sendMessage("loc delete")),
                                    literal("tp", (p, s) -> s.sendMessage("loc tp")),
                                    literal("move", (p, s) -> s.sendMessage("loc move"))
                            )
                    ),
                    literal("menu", (p, s) -> s.sendMessage("menu help"),
                            stringArg("name", StringArgNode.Type.WORD, (p, s) -> s.sendMessage("menu specific help"),
                                    literal("title", stringArg("title", StringArgNode.Type.GREEDY, (p, s) -> s.sendMessage("set title"))),
                                    literal("open", (p, s) -> s.sendMessage("menu open"),
                                            stringArg("players", StringArgNode.Type.OPTIONAL_GREEDY, (p, s) -> s.sendMessage("menu open for others"))
                                    ),
                                    literal("delete", (p, s) -> s.sendMessage("menu delete"))
                            )
                    ),
                    literal("list", (p, s) -> s.sendMessage("list help"),
                            literal("activators", (p, s) -> s.sendMessage("list activators"),
                                    stringArg("group", StringArgNode.Type.WORD, (p, s) -> s.sendMessage("list activators " + p))
                            ),
                            literal("locations", (p, s) -> s.sendMessage("list locations"),
                                    stringArg("world", StringArgNode.Type.WORD, (p, s) -> s.sendMessage("list locations " + p))
                            ),
                            literal("menus", (p, s) -> s.sendMessage("list menus"))
                    ),
                    literal("reload", (p, s) -> s.sendMessage("reload all"),
                            stringArg("options", StringArgNode.Type.WORD, (p, s) -> s.sendMessage("reload specific " + p))
                    )
            );
    }

    private void help(@NotNull Parameters params, @NotNull CommandSender sender) {
        sendHelp(sender, params, null,
                "create", "&7(&aactivator&7|&alocation&7|&amenu&7)&e <name>", "Create a new &enamed &aobject",
                "activator", "&a<name>", "Manage &anamed&r activator",
                "location", "&a<name>", "Manage &anamed&r location",
                "menu", "&a<name>", "Manage &anamed&r menu",
                // TODO variables
                "list", "&7(&aactivators&7|&alocations&7|&amenus&7)", "List &aobjects", // TODO List activities
                "reload", "", "Reload a plugin or its specific parts"
        );
    }
}
