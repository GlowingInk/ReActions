package fun.reactions.commands.plugin.impl.sub;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.plugin.RaCommandBase;
import fun.reactions.time.timers.Timer;
import fun.reactions.time.timers.TimersManager;
import fun.reactions.util.parameter.Parameters;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class ReaTimerSub extends RaCommandBase {
    public ReaTimerSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    @Override
    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("timer")
                .requires(permission("reactions.timer"))
                .then(argument("name", StringArgumentType.word())
                        .suggests(suggestNamesItr(() -> Iterables.concat( // TODO This might be a shoot in the foot
                                TimersManager.getIngameTimers().keySet(),
                                TimersManager.getServerTimers().keySet()
                        ), false))
                        .executes(this::help)
                        .then(literal("create")
                                .requires(permission("reactions.timer.edit"))
                                .then(literal("ingame")
                                        .then(argument("time", StringArgumentType.word())
                                                .executes(ctx -> createIngame(ctx, false))
                                                .then(argument("paused", BoolArgumentType.bool())
                                                        .executes(ctx -> createIngame(ctx, BoolArgumentType.getBool(ctx, "paused"))))))
                                .then(literal("server")
                                        .then(argument("cron", StringArgumentType.word())
                                                .executes(ctx -> createServer(ctx, false))
                                                .then(argument("paused", BoolArgumentType.bool())
                                                        .executes(ctx -> createServer(ctx, BoolArgumentType.getBool(ctx, "paused")))))))
                        .then(literal("info")
                                .requires(permission("reactions.timer.view"))
                                .executes(this::info))
                        .then(literal("pause")
                                .requires(permission("reactions.timer.edit"))
                                .executes(ctx -> setPaused(ctx, true)))
                        .then(literal("resume")
                                .requires(permission("reactions.timer.edit"))
                                .executes(ctx -> setPaused(ctx, false)))
                        .then(literal("delete")
                                .requires(permission("reactions.timer.edit"))
                                .executes(this::delete)))
                .build();
    }

    private int help(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        return sendHelp(ctx, "timer", name, TimersManager.isTimerExists(name), "create",
                "create", "ingame &e<activator> &6<time> &o[<paused>]", "Create in-game timer using&6 in-game time&r that runs&e activator",
                "create", "server &e<activator> &6<cron> &o[<paused>]", "Create server timer using&e cron syntax&r that runs&e activator",
                "info", "", "Get info about a timer",
                "pause", "", "Pause a timer",
                "resume", "", "Unpause a timer",
                "delete", "", "Delete a timer"
        );
    }

    private int createIngame(@NotNull CommandContext<CommandSourceStack> ctx, boolean paused) {
        String time = StringArgumentType.getString(ctx, "time");
        String activator = StringArgumentType.getString(ctx, "activator");
        return create(ctx, "INGAME", time, activator, paused);
    }

    private int createServer(@NotNull CommandContext<CommandSourceStack> ctx, boolean paused) {
        String cron = StringArgumentType.getString(ctx, "cron");
        String activator = StringArgumentType.getString(ctx, "activator");
        return create(ctx, "SERVER", cron, activator, paused);
    }

    private int create(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String timerType, @NotNull String time, @NotNull String activator, boolean paused) {
        String name = StringArgumentType.getString(ctx, "name");
        Map<String, String> params = Map.of(
                "timer-type", timerType,
                "time", time,
                "paused", String.valueOf(paused),
                "activator", activator
        );
        switch (TimersManager.addTimer(name, Parameters.fromMap(params), true)) {
            case SUCCESS -> sendPrefixed(ctx, "Timer &a'" + esc(name) + "'&r was created.");
            case ERROR_NAME_MISSING -> sendPrefixed(ctx, "Cannot create a timer with no name.");
            case ERROR_EXISTS -> sendAlreadyExists(ctx, "Timer", name);
            case ERROR_PARAMETERS_MISSING -> sendPrefixed(ctx, "Cannot create a timer with no parameters.");
            case ERROR_ACTIVATOR_MISSING -> sendPrefixed(ctx, "Cannot create a timer with no activator attached.");
            case ERROR_TYPE_MISSING -> sendPrefixed(ctx, "Cannot create a timer with no timer type.");
            case ERROR_TIME_MISSING -> sendPrefixed(ctx, "Cannot create a timer with no time conditions.");
        }
        return SINGLE_SUCCESS;
    }

    private int info(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (isTimerMissing(ctx, name)) return SINGLE_SUCCESS;

        Timer timer = getTimer(name);
        sendPrefixed(ctx, "Timer &a'" + esc(name) + "'&r : &a" + timer); // TODO Better message; time until next run
        return SINGLE_SUCCESS;
    }

    private int setPaused(@NotNull CommandContext<CommandSourceStack> ctx, boolean paused) {
        String name = StringArgumentType.getString(ctx, "name");
        if (isTimerMissing(ctx, name)) return SINGLE_SUCCESS;

        TimersManager.setPause(name, paused);
        sendPrefixed(ctx, "Timer&a '" + esc(name) + "'&r was" + (paused ? "&c paused" : "&a resumed") + "&r.");
        return SINGLE_SUCCESS;
    }

    private int delete(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (isTimerMissing(ctx, name)) return SINGLE_SUCCESS;

        TimersManager.removeTimer(ctx.getSource().getSender(), name);
        sendPrefixed(ctx, "Timer &a'" + esc(name) + "'&r was deleted.");
        return SINGLE_SUCCESS;
    }

    private boolean isTimerMissing(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String name) {
        if (TimersManager.isTimerExists(name)) return false;
        sendNotFound(ctx, "Timer", name);
        return true;
    }

    private @Nullable Timer getTimer(@NotNull String name) {
        Timer timer = TimersManager.getIngameTimers().get(name);
        return timer != null ? timer : TimersManager.getServerTimers().get(name);
    }
}