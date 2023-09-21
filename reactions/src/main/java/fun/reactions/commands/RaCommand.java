package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.commands.nodes.Node;
import fun.reactions.util.parameter.Parameters;
import ink.glowing.text.InkyMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static fun.reactions.commands.nodes.Node.LABEL_KEY;
import static ink.glowing.text.InkyMessage.inkyMessage;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;

public abstract class RaCommand {
    protected final ReActions.Platform platform;

    protected RaCommand(@NotNull ReActions.Platform platform) {
        this.platform = platform;
    }

    public abstract @NotNull Node asNode();

    protected void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(inky(message));
    }

    protected void sendPrefixed(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(inky("&6&lReA> ", message));
    }

    // subcommand, args, description
    protected void sendHelp(@NotNull CommandSender sender, @NotNull Parameters params, @NotNull String command, @NotNull String... help) {
        String label = params.getString(LABEL_KEY);
        sender.sendMessage("");
        inky(sender, "&6&l/" + label + " " + command + " ...");
        for (int i = 0; i + 2 < help.length; i += 3) {
            String subcommand = help[i];
            String args = help[i + 1];
            String description = help[i + 2];

            Component message = inky("  " + subcommand + " " + args)
                    .clickEvent(suggestCommand("/" + label + " " + command + " " + subcommand + " "))
                    .hoverEvent(inky(description));

            sender.sendMessage(message);
        }
        sender.sendMessage("");
        inky(sender, "&[&eⓘ &7Hover on commands to see the description](hover:text ... and click on them to type them in chat!");
    }

    protected @NotNull Component inky(@NotNull String str) {
        return inkyMessage().deserialize(str);
    }

    protected @NotNull Component inky(@NotNull String... strs) {
        var inky = inkyMessage();
        var text = text();
        for (var str : strs) {
            text.append(inky.deserialize(str));
        }
        return text.build();
    }

    protected void inky(@NotNull CommandSender sender, @NotNull String str) {
        sender.sendMessage(inkyMessage().deserialize(str));
    }

    protected @NotNull String escape(@NotNull String str) {
        return InkyMessage.escape(str);
    }
}
