package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.commands.nodes.Node;
import fun.reactions.util.parameter.Parameters;
import ink.glowing.text.InkyMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static fun.reactions.commands.nodes.Node.LABEL_KEY;
import static ink.glowing.text.InkyMessage.inkyMessage;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;

public abstract class RaCommand {
    private static final Component REA_PREFIX = inky("&6&lReA>&r ");

    protected final ReActions.Platform platform;

    protected RaCommand(@NotNull ReActions.Platform platform) {
        this.platform = platform;
    }

    public abstract @NotNull Node asNode();

    protected static void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(inky(message));
    }

    protected static void sendPrefixed(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(REA_PREFIX.append(inky(message)));
    }

    // subcommand, args, description
    protected static void sendHelp(@NotNull CommandSender sender, @NotNull Parameters params, @Nullable String command, @NotNull String... help) {
        String start = "/" + params.getString(LABEL_KEY) + (command != null ? " " + command : "");
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
        sendInky(sender, "&[&eⓘ &7Hover on commands to see the description](hover:text ... and click on them to type them in chat!");
    }

    protected static @NotNull Component inky(@NotNull String str) {
        return inkyMessage().deserialize(str);
    }

    protected static @NotNull Component inky(@NotNull String... strs) {
        var inky = inkyMessage();
        var text = text();
        for (var str : strs) {
            text.append(inky.deserialize(str));
        }
        return text.build();
    }

    protected static void sendInky(@NotNull CommandSender sender, @NotNull String str) {
        sender.sendMessage(inkyMessage().deserialize(str));
    }

    protected static @NotNull String escape(@NotNull String str) {
        return InkyMessage.escape(str);
    }

    protected static void exception(@NotNull String message) {
        throw new RaCommandException(message);
    }

    protected static void exception(@NotNull Component message) {
        throw new RaCommandException(message);
    }

    protected static <T> @NotNull T ensure(@Nullable T obj, @NotNull String message) {
        return ensure(obj, inky(message));
    }

    protected static <T> @NotNull T ensure(@Nullable T obj, @NotNull Component message) {
        if (obj != null) {
            return obj;
        }
        throw new RaCommandException(message);
    }

    protected static <T> @NotNull T ensurePrefixed(@Nullable T obj, @NotNull String message) {
        return ensurePrefixed(obj, inky(message));
    }

    protected static <T> @NotNull T ensurePrefixed(@Nullable T obj, @NotNull Component message) {
        if (obj != null) {
            return obj;
        }
        throw new RaCommandException(REA_PREFIX.append(message));
    }
}
