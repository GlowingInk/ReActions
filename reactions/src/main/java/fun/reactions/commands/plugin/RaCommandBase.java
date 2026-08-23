package fun.reactions.commands.plugin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import ink.glowing.text.InkyMessage;
import ink.glowing.text.placeholder.Placeholder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static fun.reactions.util.Utils.upperFirst;
import static ink.glowing.text.InkyMessage.inkyMessage;
import static ink.glowing.text.placeholder.Placeholder.placeholder;
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

    protected static int sendHelp(@NotNull CommandContext<CommandSourceStack> ctx, @Nullable String command, @NotNull String... help) {
        CommandSender sender = ctx.getSource().getSender();
        boolean isPlayer = sender instanceof Player;

        String start = "/" + rootLabel(ctx) + (command != null ? " " + command : "");
        sender.sendMessage("");
        sendInky(sender, "&6&l" + start + " ...");
        for (int i = 0; i + 2 < help.length; i += 3) {
            String subcommand = help[i];
            String args = help[i + 1];
            String description = help[i + 2];

            if (isPlayer) {
                Component message = inky("  " + subcommand + " " + args)
                        .clickEvent(suggestCommand(start + " " + subcommand + " "))
                        .hoverEvent(inky(description));
                sender.sendMessage(message);
            } else {
                sendInky(sender, "  &7" + subcommand + " " + args + "&r &8-&r &7" + description);
            }
        }
        sender.sendMessage("");
        if (isPlayer) {
            sendInky(sender, "&[&eⓘ &7Hover on commands to see the description](hover:text ... and click on them to type in chat!");
        }
        return SINGLE_SUCCESS;
    }

    protected static int promptForName(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String object) {
        sendPrefixed(ctx, "Type in a " + object + "&e name&r to continue.");
        return SINGLE_SUCCESS;
    }

    protected static int sendHelp(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull String object,
            @NotNull String name,
            boolean exists,
            @NotNull String createSubcommand,
            @NotNull String... help
    ) {
        if (!exists) {
            CommandSender sender = ctx.getSource().getSender();
            String start = "/" + rootLabel(ctx) + " " + object + " " + name + " " + createSubcommand;

            sendPrefixed(sender, upperFirst(object) + " &c'" + esc(name) + "'&r doesn't exist.");
            if (sender instanceof Player) {
                sendInky(sender, "&[  &aClick here to create it](click:suggest " + start + " )(hover:text &7" + start + ")");
            } else {
                sendInky(sender, "  &7Use &a" + start + "&r to create it.");
            }
            return SINGLE_SUCCESS;
        }
        return sendHelp(ctx, object + " " + esc(name), help);
    }

    protected static void sendNotFound(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String object, @NotNull String name) {
        sendPrefixed(ctx, object + " &c'" + esc(name) + "'&r doesn't exist.");
    }

    protected static void sendAlreadyExists(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String object, @NotNull String name) {
        sendPrefixed(ctx, object + " &c'" + esc(name) + "'&r already exists.");
    }

    protected static @NotNull SuggestionProvider<CommandSourceStack> suggestNames(@NotNull Supplier<? extends Collection<String>> names, boolean star) {
        return (_, builder) -> {
            String remaining = builder.getRemaining();
            if (star && "*".startsWith(remaining)) builder.suggest("*");
            names.get().stream()
                    .filter(s -> s.startsWith(remaining))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    protected static int sendPage(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull String command,
            @NotNull String title,
            @NotNull List<String> lines,
            int page
    ) {
        CommandSender sender = ctx.getSource().getSender();
        if (lines.isEmpty()) {
            sendPrefixed(sender, "No " + title.toLowerCase(Locale.ROOT) + " found.");
            return SINGLE_SUCCESS;
        }

        int linesPerPage = sender instanceof Player ? 15 : lines.size();
        int totalPages = (lines.size() + linesPerPage - 1) / linesPerPage;
        int current = Math.clamp(page, 1, totalPages);
        int from = (current - 1) * linesPerPage;
        int to = Math.min(from + linesPerPage, lines.size());

        sender.sendMessage("");
        sendInky(sender, "&6&l" + title + "&7 (" + current + "/" + totalPages + "):");
        for (String line : lines.subList(from, to)) {
            sendInky(sender, "  " + line);
        }
        if (totalPages > 1 && sender instanceof Player) {
            String start = "/" + rootLabel(ctx) + " " + command + " ";
            String prev = current > 1
                    ? "&[&a« Prev](click:run " + start + (current - 1) + ")"
                    : "&7« Prev";
            String next = current < totalPages
                    ? "&[&aNext »](click:run " + start + (current + 1) + ")"
                    : "&7Next »";
            sendInky(sender, "  " + prev + " &7[&f" + current + "&7/&f" + totalPages + "&7]&r " + next);
        }
        return SINGLE_SUCCESS;
    }

    protected static @NotNull Predicate<CommandSourceStack> permission(@NotNull String permission) {
        return source -> source.getSender().hasPermission(permission);
    }

    protected static @NotNull String rootLabel(@NotNull CommandContext<CommandSourceStack> ctx) {
        int spaceAt = ctx.getInput().indexOf(' ');
        return spaceAt == -1
                ? ctx.getInput()
                : ctx.getInput().substring(0, spaceAt);
    }

    protected static @NotNull Component inky(@NotNull String str) {
        return inkyMessage().deserialize(str);
    }

    protected static @NotNull Component inky(@NotNull String str, @NotNull Map<String, ?> placeholdersMap) {
        List<Placeholder> placeholders = new ArrayList<>(placeholdersMap.size());
        for (var entry : placeholdersMap.entrySet()) {
            if (entry.getValue() instanceof Component comp) {
                placeholders.add(placeholder(entry.getKey(), comp));
            } else {
                placeholders.add(placeholder(entry.getKey(), text(String.valueOf(entry.getValue()).replace('§', '&'))));

            }
        }
        return inkyMessage().deserialize(str, placeholders);
    }

    protected static void sendInky(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String str, @NotNull Map<String, ?> placeholdersMap) {
        sendInky(ctx.getSource().getSender(), str, placeholdersMap);
    }

    protected static void sendInky(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String str) {
        sendInky(ctx.getSource().getSender(), str);
    }

    protected static void sendInky(@NotNull CommandSender sender, @NotNull String str, @NotNull Map<String, ?> placeholdersMap) {
        sender.sendMessage(inky(str, placeholdersMap));
    }

    protected static void sendInky(@NotNull CommandSender sender, @NotNull String str) {
        sender.sendMessage(inky(str));
    }

    protected static void sendPrefixed(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String message, @NotNull Map<String, ?> placeholdersMap) {
        sendPrefixed(ctx.getSource().getSender(), message, placeholdersMap);
    }

    protected static void sendPrefixed(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String message) {
        sendPrefixed(ctx.getSource().getSender(), message);
    }

    protected static void sendPrefixed(@NotNull CommandSender sender, @NotNull String message, @NotNull Map<String, ?> placeholdersMap) {
        sender.sendMessage(REA_PREFIX.append(inky(message, placeholdersMap)));
    }

    protected static void sendPrefixed(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(REA_PREFIX.append(inky(message)));
    }

    protected static @NotNull String esc(@NotNull String str) {
        return InkyMessage.escape(str);
    }
}
