package fun.reactions.commands.plugin.impl.sub;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.Cfg;
import fun.reactions.ReActions;
import fun.reactions.commands.plugin.RaCommandBase;
import fun.reactions.holders.LocationHolder;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.module.worldguard.external.RaWorldGuard;
import fun.reactions.time.CooldownManager;
import fun.reactions.time.timers.TimersManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaReloadSub extends RaCommandBase {
    private static final Set<String> TARGETS = Set.of(
            "activators", "locations", "config", "cooldowns", "variables", "timers", "menus"
    );

    public ReaReloadSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("reload")
                .requires(permission("reactions.reload"))
                .executes(this::reloadAll)
                .then(argument("targets", StringArgumentType.greedyString())
                        .suggests(this::suggestTargets)
                        .executes(this::reloadTargets))
                .build();
    }

    private int reloadAll(@NotNull CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        List<String> done = new ArrayList<>();
        List<String> denied = new ArrayList<>();
        for (String target : TARGETS) {
            if (!sender.hasPermission(permissionFor(target))) {
                denied.add(target);
                continue;
            }
            done.add(describeTarget(target, runTarget(target)));
        }
        reportResult(ctx, done, denied, List.of());
        return SINGLE_SUCCESS;
    }

    private int reloadTargets(@NotNull CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        String raw = StringArgumentType.getString(ctx, "targets");
        List<String> done = new ArrayList<>();
        List<String> denied = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        for (String segment : raw.split("&")) {
            String trimmed = segment.trim();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split("\\s+", 2);
            String keyword = parts[0].toLowerCase(Locale.ROOT);
            String arg = parts.length > 1 ? parts[1].trim() : "";
            if (!TARGETS.contains(keyword)) {
                unknown.add(parts[0]);
                continue;
            }
            if (!sender.hasPermission(permissionFor(keyword))) {
                denied.add(keyword);
                continue;
            }
            if (keyword.equals("activators") && !arg.isEmpty()) {
                int amount = doReloadActivatorGroup(arg);
                int groups = countGroupsUnder(arg);
                done.add("activators " + esc(arg) + describeAmount(amount, groups));
            } else {
                done.add(describeTarget(keyword, runTarget(keyword)));
            }
        }
        reportResult(ctx, done, denied, unknown);
        return SINGLE_SUCCESS;
    }

    private @NotNull String describeTarget(@NotNull String target, int amount) {
        if (target.equals("activators")) {
            return target + describeAmount(amount, platform.getActivators().getGroupNames().size());
        }
        return amount < 0 ? target : target + " (&e" + amount + "&r)";
    }

    private static @NotNull String describeAmount(int amount, int groups) {
        return " (&e" + amount + "&r in &e" + groups + "&r group" + (groups == 1 ? "" : "s") + ")";
    }

    private int countGroupsUnder(@NotNull String rawGroup) {
        String group = rawGroup.replaceAll("[/\\\\]", Matcher.quoteReplacement(File.separator));
        int count = 0;
        for (String g : platform.getActivators().getGroupNames()) {
            if (g.equals(group) || g.startsWith(group + File.separator)) count++;
        }
        return count;
    }

    private void reportResult(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull List<String> done,
            @NotNull List<String> denied,
            @NotNull List<String> unknown
    ) {
        if (done.isEmpty() && denied.isEmpty() && unknown.isEmpty()) {
            sendPrefixed(ctx, "Nothing to reload - specify&c at least&r one target.");
            return;
        }
        if (!done.isEmpty()) {
            sendPrefixed(ctx, "Reloaded &a" + String.join("&r, &a", done) + "&r.");
        }
        for (String d : denied) {
            sendPrefixed(ctx, "No permission to reload &c'" + esc(d) + "'&r.");
        }
        for (String u : unknown) {
            sendPrefixed(ctx, "Unknown reload target &c'" + esc(u) + "'&r.");
        }
    }

    private @NotNull CompletableFuture<Suggestions> suggestTargets(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull SuggestionsBuilder builder
    ) {
        CommandSender sender = ctx.getSource().getSender();
        String remaining = builder.getRemaining();
        int lastAmp = remaining.lastIndexOf('&');
        String before = lastAmp == -1 ? "" : remaining.substring(0, lastAmp + 1);
        String segment = remaining.substring(lastAmp + 1);
        String trimmedSegment = segment.stripLeading();
        int trimmedStartAbs = builder.getStart() + lastAmp + 1 + (segment.length() - trimmedSegment.length());

        int spaceIdx = trimmedSegment.indexOf(' ');
        if (spaceIdx == -1) {
            Set<String> used = usedTargets(before);
            SuggestionsBuilder segBuilder = builder.createOffset(trimmedStartAbs);
            String wordSoFar = trimmedSegment.toLowerCase(Locale.ROOT);
            for (String target : TARGETS) {
                if (!used.contains(target) && sender.hasPermission(permissionFor(target)) && target.startsWith(wordSoFar)) {
                    segBuilder.suggest(target);
                }
            }
            return segBuilder.buildFuture();
        }

        String keyword = trimmedSegment.substring(0, spaceIdx).toLowerCase(Locale.ROOT);
        String argPart = trimmedSegment.substring(spaceIdx + 1);
        if (keyword.equals("activators") && argPart.indexOf(' ') == -1) {
            SuggestionsBuilder groupBuilder = builder.createOffset(trimmedStartAbs + spaceIdx + 1);
            if ("&".startsWith(argPart)) groupBuilder.suggest("&");
            for (String group : platform.getActivators().getGroupNames()) {
                if (group.startsWith(argPart)) groupBuilder.suggest(group);
            }
            return groupBuilder.buildFuture();
        }
        if (TARGETS.contains(keyword)) {
            SuggestionsBuilder ampBuilder = builder.createOffset(builder.getInput().length());
            ampBuilder.suggest("&");
            return ampBuilder.buildFuture();
        }
        return builder.buildFuture();
    }

    private static @NotNull Set<String> usedTargets(@NotNull String before) {
        Set<String> used = new HashSet<>();
        for (String segment : before.split("&")) {
            String trimmed = segment.trim();
            if (trimmed.isEmpty()) continue;
            used.add(trimmed.split("\\s+", 2)[0].toLowerCase(Locale.ROOT));
        }
        return used;
    }

    private static @NotNull String permissionFor(@NotNull String target) {
        return "reactions.reload." + target;
    }

    private int runTarget(@NotNull String target) {
        return switch (target) {
            case "activators" -> doReloadActivators();
            case "locations" -> doReloadLocations();
            case "config" -> doReloadConfig();
            case "cooldowns" -> doReloadCooldowns();
            case "variables" -> doReloadVariables();
            case "timers" -> doReloadTimers();
            case "menus" -> doReloadMenus();
            default -> -1;
        };
    }

    private int doReloadActivators() {
        platform.getActivators().clearActivators();
        int amount = platform.getActivators().loadGroup("", false);
        RaWorldGuard.updateRegionCache();
        return amount;
    }

    private int doReloadActivatorGroup(@NotNull String rawGroup) {
        String group = rawGroup.replaceAll("[/\\\\]", Matcher.quoteReplacement(File.separator));
        int amount = platform.getActivators().loadGroup(group, true);
        RaWorldGuard.updateRegionCache();
        return amount;
    }

    private int doReloadLocations() {
        LocationHolder.loadLocs();
        return LocationHolder.sizeTpLoc();
    }

    private int doReloadConfig() {
        platform.getPlugin().reloadConfig();
        Cfg.load(platform.getPlugin().getConfig());
        platform.getCommands().reload();
        return -1;
    }

    private int doReloadCooldowns() {
        CooldownManager.load();
        return CooldownManager.size();
    }

    private int doReloadVariables() {
        if (!Cfg.playerSelfVarFile) platform.getPersistentVariables().load();
        else platform.getPersistentVariables().loadVars();
        return platform.getPersistentVariables().size();
    }

    private int doReloadTimers() {
        TimersManager.init();
        return TimersManager.getIngameTimers().size() + TimersManager.getServerTimers().size();
    }

    private int doReloadMenus() {
        InventoryMenu.load();
        return InventoryMenu.getMenuNames().size();
    }
}
