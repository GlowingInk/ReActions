package fun.reactions.commands.impl.sub;

import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.commands.nodes.Node;
import fun.reactions.commands.nodes.StringArgNode;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static fun.reactions.commands.nodes.LiteralNode.literal;
import static fun.reactions.commands.nodes.StringArgNode.stringArg;

public class ReaVariableSub extends RaCommandBase {
    public ReaVariableSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    @Override
    public @NotNull Node asNode() {
        return literal("variable", stringArg("name", StringArgNode.Type.WORD, this::help,
                        literal("show"/*, this::info*/),
                        literal("delete"/*, this::delete*/),
                        literal("set", /*this::teleport,*/ stringArg("value", StringArgNode.Type.OPTIONAL_GREEDY))
                )
        );
    }

    private void help(@NotNull Parameters params, @NotNull CommandSender sender) {
        sendHelp(sender, params, "variable " + escape(params.getString("name")),
                "show", "", "Show a variable",
                "delete", "", "Delete a variable",
                "set", "[value]", "Set variable to a &especified value"
        );
    }

    private void show(@NotNull Parameters params, @NotNull CommandSender sender) {
        String value = platform.getPersistentVariables().getVariable(null, params.getString("name"));
        sender.sendMessage(value == null ? "" : value);
    }
}
