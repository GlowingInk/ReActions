package fun.reactions.commands.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActionsPlugin;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.commands.impl.sub.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReactionsCommand extends RaCommandBase {
    public ReactionsCommand(@NotNull ReActionsPlugin plugin) {
        super(plugin);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("reactions")
                .executes(ctx -> {
                    help(ctx);
                    return Command.SINGLE_SUCCESS;
                })
                .then(new ReaActivatorSub(platform).asNode())
                .then(new ReaLocationSub(platform).asNode())
                .then(new ReaVariableSub(platform).asNode())
                .then(new ReaMenuSub(platform).asNode())
                .then(new ReaListSub(platform).asNode())
                .then(new ReaReloadSub(platform).asNode())
                .build();
    }

    private void help(@NotNull CommandContext<CommandSourceStack> ctx) {
        sendHelp(ctx, null,
                "activator", "&a<name>", "Manage &anamed&r activator",
                "location", "&a<name>", "Manage &anamed&r location",
                "menu", "&a<name>", "Manage &anamed&r menu",
                "variable", "&a<name>", "Manage &anamed&r variable",
                "list", "&7(&aactivators&7|&alocations&7|&amenus&7)", "List &aobjects", // TODO List activities
                "reload", "", "Reload a plugin or its specific parts"
        );
    }
}