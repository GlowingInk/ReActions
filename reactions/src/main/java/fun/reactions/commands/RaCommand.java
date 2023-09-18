package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.commands.nodes.Node;
import org.jetbrains.annotations.NotNull;

public abstract class RaCommand {
    protected final ReActions.Platform platform;

    protected RaCommand(@NotNull ReActions.Platform platform) {
        this.platform = platform;
    }

    public abstract @NotNull Node asNode();
}
