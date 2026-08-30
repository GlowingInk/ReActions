package fun.reactions.commands.plugin.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActionsPlugin;
import fun.reactions.commands.plugin.RaCommandBase;
import fun.reactions.commands.plugin.impl.sub.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReactionsCommand extends RaCommandBase {
    public ReactionsCommand(@NotNull ReActionsPlugin plugin) {
        super(plugin);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("reactions")
                .requires(permission("reactions"))
                .executes(ctx -> {
                    help(ctx);
                    return SINGLE_SUCCESS;
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
                "activator", "&a<name>", "Manage&a named&r activator",
                "location", "&a<name>", "Manage&a named&r location",
                "menu", "&a<name>", "Manage&a named&r menu",
                "timer", "&a<name>", "Manage&a named&r timer",
                "variable", "&a<name>", "Manage&a named&r variable",
                "list", "&7(&aactivators&7|&alocations&7|&amenus&7)", "List&a objects",
                // TODO List activities, placeholders, selectors and whatnot
                "reload", "", "Reload a plugin or its specific parts"
        );
    }
}