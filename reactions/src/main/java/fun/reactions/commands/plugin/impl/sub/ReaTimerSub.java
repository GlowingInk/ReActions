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
import fun.reactions.util.time.TimeUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaTimerSub extends RaCommandBase {
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
                                        .then(argument("activator", StringArgumentType.word())
                                                .suggests(suggestNames(() -> platform.getActivators().getActivatorNames(), false))
                                                .then(argument("time", StringArgumentType.word())
                                                        .executes(ctx -> createIngame(ctx, false, null))
                                                        .then(argument("paused", BoolArgumentType.bool())
                                                                .executes(ctx -> createIngame(ctx, BoolArgumentType.getBool(ctx, "paused"), null))
                                                                .then(argument("world", ArgumentTypes.world())
                                                                        .executes(ctx -> createIngame(
                                                                                ctx,
                                                                                BoolArgumentType.getBool(ctx, "paused"),
                                                                                ctx.getArgument("world", World.class)
                                                                        )))))))
                                .then(literal("server")
                                        .then(argument("activator", StringArgumentType.word())
                                                .suggests(suggestNames(() -> platform.getActivators().getActivatorNames(), false))
                                                .then(argument("cron", StringArgumentType.word())
                                                        .executes(ctx -> createServer(ctx, false))
                                                        .then(argument("paused", BoolArgumentType.bool())
                                                                .executes(ctx -> createServer(ctx, BoolArgumentType.getBool(ctx, "paused"))))))))
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
                "create", "ingame &a<activator> &e<time> &6[<paused>] [<world>]", "Create in-game timer using&e in-game time&r of the world that runs&a activator",
                "create", "server &a<activator> &e<cron> &6[<paused>]", "Create server timer using&e cron syntax&r that runs&a activator",
                "info", "", "Get info about a timer",
                "pause", "", "Pause a timer",
                "resume", "", "Unpause a timer",
                "delete", "", "Delete a timer"
        );
    }

    private int createIngame(@NotNull CommandContext<CommandSourceStack> ctx, boolean paused, @Nullable World world) {
        String time = StringArgumentType.getString(ctx, "time");
        String activator = StringArgumentType.getString(ctx, "activator");
        return create(ctx, "INGAME", time, activator, paused, world == null ? null : world.getName());
    }

    private int createServer(@NotNull CommandContext<CommandSourceStack> ctx, boolean paused) {
        String cron = StringArgumentType.getString(ctx, "cron");
        String activator = StringArgumentType.getString(ctx, "activator");
        return create(ctx, "SERVER", cron, activator, paused, null);
    }

    private int create(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull String timerType,
            @NotNull String time,
            @NotNull String activator,
            boolean paused,
            @Nullable String world
    ) {
        String name = StringArgumentType.getString(ctx, "name");
        Map<String, String> params = new HashMap<>();
        params.put("timer-type", timerType);
        params.put("time", time);
        params.put("paused", String.valueOf(paused));
        params.put("activator", activator);
        if (world != null) params.put("world", world);
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
        Parameters params = timer.getParams();
        sendInky(ctx, "&6&l" + esc(name) + "&e (" + timer.getType() + ")" + (timer.isPaused() ? " &c[paused]" : ""));
        sendInky(ctx, " &eActivator &7= &r" + esc(params.getString("activator", "")));
        sendInky(ctx, " &eTime &7= &r" + esc(params.getString("time", "")));
        if (timer instanceof Timer.Ingame ingame) {
            sendInky(ctx, " &eWorld &7= &r" + (ingame.getWorld() == null ? "&7main" : esc(ingame.getWorld())));
        }
        sendInky(ctx, " &eNext run &7= &r" + describeNextRun(timer));
        return SINGLE_SUCCESS;
    }

    private static @NotNull String describeNextRun(@NotNull Timer timer) {
        if (timer.isPaused()) return "&7paused";
        Duration until = timer.untilNextRun();
        if (until == null) return "&cnever";
        String in = "in " + TimeUtils.formatDuration(until.toMillis());
        return timer instanceof Timer.Ingame
                ? in + " &7(approximately)"
                : in + " &7(" + TimeUtils.formatTime(System.currentTimeMillis() + until.toMillis()) + ")";
    }

    private int setPaused(@NotNull CommandContext<CommandSourceStack> ctx, boolean paused) {
        String name = StringArgumentType.getString(ctx, "name");
        if (isTimerMissing(ctx, name)) return SINGLE_SUCCESS;

        TimersManager.setPause(name, paused);
        sendPrefixed(ctx, "Timer&a '" + esc(name) + "'&r was" + (paused ? "&e paused" : "&e resumed") + "&r.");
        return SINGLE_SUCCESS;
    }

    private int delete(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (TimersManager.removeTimer(name)) {
            sendPrefixed(ctx, "Timer &a'" + esc(name) + "'&r was deleted.");
        } else {
            sendNotFound(ctx, "Timer", name);
        }
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