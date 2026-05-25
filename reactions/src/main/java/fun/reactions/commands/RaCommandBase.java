package fun.reactions.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import ink.glowing.text.InkyMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static ink.glowing.text.InkyMessage.inkyMessage;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;

@ApiStatus.Internal
public abstract class RaCommandBase {
    private static final Component REA_PREFIX = inky("&6&lReA>&r ");

    protected final ReActions.Platform platform;

    protected RaCommandBase(@NotNull ReActions.Platform platform) {
        this.platform = platform;
    }

    public abstract @NotNull LiteralCommandNode<CommandSourceStack> asNode();

    protected static void sendPrefixed(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(REA_PREFIX.append(inky(message)));
    }

    protected static int sendHelp(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable String command, @NotNull String... help) {
        CommandSender sender = ctx.getSource().getSender();
        String label = ctx.getRootNode().getName(); // TODO Actual label from ctx.getInput()?

        String start = "/" + label + (command != null ? " " + command : "");
        sender.sendMessage("");
        sendInky(sender, "&6&l" + start + " ...");
        for (int i = 0; i + 2 < help.length; i += 3) {
            String subcommand = help[i];
            String args = help[i + 1];
            String description = help[i + 2];

            Component message = inky("  " + subcommand + " " + args)
                    .clickEvent(suggestCommand(start + " " + subcommand + " "))
                    .hoverEvent(inky(description));

            sender.sendMessage(message);
        }
        sender.sendMessage("");
        sendInky(sender, "&[&eⓘ &7Hover on commands to see the description](hover:text ... and click on them to type in chat!");
        return Command.SINGLE_SUCCESS;
    }

    protected static @NotNull Component inky(@NotNull String str) {
        return inkyMessage().deserialize(str);
    }

    protected static @NotNull Component inky(@NotNull Object... objs) {
        var inky = inkyMessage();
        var text = text();
        for (var obj : objs) {
            text.append(inky.deserialize(String.valueOf(obj)));
        }
        return text.build();
    }

    protected static void sendInky(@NotNull CommandSender sender, @NotNull String str) {
        sender.sendMessage(inkyMessage().deserialize(str));
    }

    protected static @NotNull String escape(@NotNull String str) {
        return InkyMessage.escape(str);
    }

    protected static void exception(@NotNull String message) throws ComponentException {
        throw new ComponentException(message);
    }

    protected static void exception(@NotNull Component message) throws ComponentException {
        throw new ComponentException(message);
    }

    @Contract("null, _ -> fail")
    protected static <T> @NotNull T ensure(@Nullable T obj, @NotNull String message) {
        if (obj != null) {
            return obj;
        }
        throw new ComponentException(REA_PREFIX.append(inky(message)));
    }

    @Contract("null, _ -> fail")
    protected static <T> @NotNull T ensure(@Nullable T obj, @NotNull Component message) {
        if (obj != null) {
            return obj;
        }
        throw new ComponentException(REA_PREFIX.append(message));
    }

    @Contract("false, _ -> fail")
    protected static void ensure(boolean v, @NotNull String message) {
        if (!v) {
            throw new ComponentException(REA_PREFIX.append(inky(message)));
        }
    }

    @Contract("false, _ -> fail")
    protected static void ensure(boolean v, @NotNull Component message) {
        if (!v) {
            throw new ComponentException(REA_PREFIX.append(message));
        }
    }
}
