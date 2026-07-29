package fun.reactions.commands.impl.sub;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.Cfg;
import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
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

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaReloadSub extends RaCommandBase {
    private static final List<String> TARGETS = List.of(
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
            runTarget(target);
            done.add(target);
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
                doReloadActivatorGroup(arg);
                done.add("activators " + arg);
            } else {
                runTarget(keyword);
                done.add(keyword);
            }
        }
        reportResult(ctx, done, denied, unknown);
        return SINGLE_SUCCESS;
    }

    private void reportResult(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull List<String> done,
            @NotNull List<String> denied,
            @NotNull List<String> unknown
    ) {
        if (!done.isEmpty()) {
            sendPrefixed(ctx, "Reloaded &a" + String.join("&r, &a", done.stream().map(ReaReloadSub::esc).toList()) + "&r.");
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

    private void runTarget(@NotNull String target) {
        switch (target) {
            case "activators" -> doReloadActivators();
            case "locations" -> doReloadLocations();
            case "config" -> doReloadConfig();
            case "cooldowns" -> doReloadCooldowns();
            case "variables" -> doReloadVariables();
            case "timers" -> doReloadTimers();
            case "menus" -> doReloadMenus();
        }
    }

    private void doReloadActivators() {
        platform.getActivators().clearActivators();
        platform.getActivators().loadGroup("", false);
        RaWorldGuard.updateRegionCache();
    }

    private void doReloadActivatorGroup(@NotNull String rawGroup) {
        String group = rawGroup.replaceAll("[/\\\\]", File.separator);
        platform.getActivators().loadGroup(group, true);
        RaWorldGuard.updateRegionCache();
    }

    private void doReloadLocations() {
        LocationHolder.loadLocs();
    }

    private void doReloadConfig() {
        platform.getPlugin().reloadConfig();
        Cfg.load(platform.getPlugin().getConfig());
        platform.getCommands().reload();
    }

    private void doReloadCooldowns() {
        CooldownManager.load();
    }

    private void doReloadVariables() {
        if (!Cfg.playerSelfVarFile) platform.getPersistentVariables().load();
        else platform.getPersistentVariables().loadVars();
    }

    private void doReloadTimers() {
        TimersManager.init();
    }

    private void doReloadMenus() {
        InventoryMenu.load();
    }
}
