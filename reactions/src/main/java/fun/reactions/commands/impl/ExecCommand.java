package fun.reactions.commands.impl;

import fun.reactions.ReActionsPlugin;
import fun.reactions.commands.RaCommand;
import fun.reactions.commands.nodes.Node;
import fun.reactions.commands.nodes.StringArgNode;
import fun.reactions.module.basic.ContextManager;
import fun.reactions.util.parameter.Parameters;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static fun.reactions.commands.nodes.CommandNode.command;
import static fun.reactions.commands.nodes.StringArgNode.stringArg;

public class ExecCommand extends RaCommand {
    private final Commodore commodore;
    private final PluginCommand execCommand;

    public ExecCommand(@NotNull ReActionsPlugin plugin) {
        super(plugin);
        this.commodore = CommodoreProvider.getCommodore(plugin);
        this.execCommand = Objects.requireNonNull(plugin.getCommand("exec"));
    }

    @Override
    public @NotNull Node asNode() {
        return command(commodore, execCommand, this::help,
                stringArg("activator", StringArgNode.Type.WORD, this::activate,
                        stringArg("parameters", StringArgNode.Type.OPTIONAL_GREEDY)
                )
        );
    }

    private void help(Parameters params, CommandSender sender) {
        sendHelp(sender, params, null, "",
                "", "<activator> [player:<selector>] [delay:<time>]", "Execute FUNCTION &aactivator&r."
        );
    }

    private void activate(Parameters params, CommandSender sender) {
        ContextManager.triggerFunction(sender, params.getParameters("parameters"));
    }
}
