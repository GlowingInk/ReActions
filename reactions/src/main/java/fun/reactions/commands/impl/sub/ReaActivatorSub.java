package fun.reactions.commands.impl.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activators.type.ActivatorTypesRegistry;
import fun.reactions.model.activity.ActivitiesRegistry;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.util.parameter.Parameters;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;

public final class ReaActivatorSub extends RaCommandBase {
    private final ActivatorsManager activators;
    private final ActivitiesRegistry activities;
    private final ActivatorTypesRegistry types;

    public ReaActivatorSub(@NotNull ReActions.Platform platform) {
        super(platform);
        this.activators = platform.getActivators();
        this.activities = platform.getActivities();
        this.types = platform.getActivatorTypes();
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("activator")
                .then(argument("name", StringArgumentType.word())
                        .suggests((_, builder) -> {
                            activators.getActivatorNames().stream()
                                    .filter(s -> s.startsWith(builder.getRemaining()))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(this::help)
                        .then(literal("create")
                                .then(argument("type", StringArgumentType.word())
                                        .suggests((_, builder) -> {
                                            types.getTypeNames().stream()
                                                    .filter(s -> s.startsWith(builder.getRemaining()))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            createActivator(ctx, "");
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(argument("parameters", StringArgumentType.greedyString()) // TODO Distant future: provide params
                                                .executes(ctx -> {
                                                    createActivator(ctx, StringArgumentType.getString(ctx, "parameters"));
                                                    return Command.SINGLE_SUCCESS;
                                                }))))
                        .then(literal("info").executes(this::info))
                        .then(literal("delete").executes(this::deletePrompt)
                                .then(literal("confirm").executes(this::delete)))
                        .then(activityNode(ActivitySelection.ACTION, activities::getActionsTypesNames))
                        .then(activityNode(ActivitySelection.REACTION, activities::getActionsTypesNames))
                        .then(activityNode(ActivitySelection.FLAG, activities::getFlagsTypesNames)))
                .build();
    }

    private @NotNull LiteralCommandNode<CommandSourceStack> activityNode(@NotNull ActivitySelection type, @NotNull Supplier<Collection<String>> suggests) {
        return literal(type.name().toLowerCase(Locale.ROOT))
                .executes(ctx -> activityHelp(ctx, type))
                .then(literal("add")
                        .then(argument("type", StringArgumentType.word())
                                .suggests((_, builder) -> {
                                    suggests.get().stream()
                                            .filter(s -> s.startsWith(builder.getRemaining()))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> activityAdd(ctx, type, ""))
                                .then(argument("parameters", StringArgumentType.greedyString())
                                        .executes(ctx -> activityAdd(ctx, type, StringArgumentType.getString(ctx, "parameters"))))))
                .then(literal("remove")
                        .then(argument("index", IntegerArgumentType.integer(1)).executes(ctx -> activityRemove(ctx, type))))
                .then(literal("move")
                        .then(argument("from", IntegerArgumentType.integer(1))
                                .then(argument("to", IntegerArgumentType.integer(1))
                                        .executes(ctx -> activityMove(ctx, type)))))
                .build();
    }

    private int help(@NotNull CommandContext<CommandSourceStack> ctx) {
        return sendHelp(ctx, "activator " + getActivator(ctx).getLogic().getName(),
                "create", "&a<type>&e [<parameters...>]", "Create this activator with specified&a type&r and&e parameters",
                "info", "", "Get info about an activator",
                "move", "&a<group>", "Move activator into another group",
                "delete", "[confirm]", "Delete an activator",
                "action", "...", "Manage activator actions",
                "reaction", "...", "Manage activator reactions",
                "flag", "...", "Manage activator flags"
        );
    }

    private void createActivator(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String rawParameters) {
        String name = StringArgumentType.getString(ctx, "name");
        String typeName = StringArgumentType.getString(ctx, "type");
        if (activators.getActivator(name) != null) {
            sendPrefixed(ctx, "Activator&c '" + esc(name) + "'&r already exists");
            return;
        }
        ActivatorType type = ensure(types.get(typeName), "Activator type&c '" + esc(typeName) + "'&r doesn't exist");
        Activator activator = ensure(
                type.createActivator(
                        new Logic(platform, type.getName(), name),
                        Parameters.fromString(rawParameters)
                ),
                "Failed to create activator&c!"
        );
        activators.addActivator(activator, true);
        sendPrefixed(ctx, "Activator&a '&{name}'&r of type&a '&{type}'&r was created", Map.of(
                "name", activator.getLogic().getName(),
                "type", activator.getLogic().getType()
        ));
    }

    private int info(@NotNull CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        Activator activator = getActivator(ctx);
        Logic logic = activator.getLogic();
        sender.sendMessage("");
        sender.sendMessage(text()
                .append(inky("&7" + logic.getGroup() + "/&6&l" + logic.getName()))
                .append(inky("&e (" + logic.getType() + ")")));
        sendActivityInfo(sender, logic.getFlags(), "&aFlags:", true);
        sendActivityInfo(sender, logic.getActions(), "&aActions:", false);
        sendActivityInfo(sender, logic.getReactions(), "&aReactions:", false);
        return Command.SINGLE_SUCCESS;
    }

    private void sendActivityInfo(CommandSender sender, List<? extends Activity.Stored<?>> storeds, String title, boolean isFlag) {
        if (storeds.isEmpty()) return;
        sendInky(sender, title);
        for (int i = 0; i < storeds.size(); i++) {
            Activity.Stored<?> storedActivity = storeds.get(i);
            Activity activity = storedActivity.getActivity();
            Component text;
            if (isFlag && storedActivity instanceof Flag.Stored storedFlag) {
                text = inky(" &7" + (i + 1) + (storedFlag.isInverted() ? " &c&l!&r" : " "));
            } else {
                text = inky(" &7" + (i + 1) + " ");
            }
            sender.sendMessage(text
                    .append(inky("&e" + activity.getName() + " &7= &r"))
                    .append(text(storedActivity.getContent())));
        }
    }

    private int deletePrompt(@NotNull CommandContext<CommandSourceStack> ctx) {
        sendPrefixed(ctx, "Add confirm to the end of a command.");
        return Command.SINGLE_SUCCESS;
    }

    private int delete(@NotNull CommandContext<CommandSourceStack> ctx) {
        Activator activator = getActivator(ctx);
        String activatorName = activator.getLogic().getName();
        activators.removeActivator(activatorName);
        sendPrefixed(ctx, "Activator &a'&{name}'&r was successfully removed.", Map.of("name", activatorName));
        saveActivator(activator);
        return Command.SINGLE_SUCCESS;
    }

    private int activityHelp(@NotNull CommandContext<CommandSourceStack> ctx, ActivitySelection selection) {
        Activator activator = getActivator(ctx);
        return sendHelp(ctx, "activator " + esc(activator.getLogic().getName()) + " " + selection,
                "add", "<type> &e[parameters...]", "Add &a" + selection + "&r to an activator",
                "remove", "<index>", "Remove &a" + selection + "&r from an activator",
                "move", "<from> <to>", "Move &a" + selection + "&r to another index"
        );
    }

    private int activityAdd(@NotNull CommandContext<CommandSourceStack> ctx, ActivitySelection selection, String parameters) {
        Activator activator = getActivator(ctx);
        String type = StringArgumentType.getString(ctx, "type");
        if (selection == ActivitySelection.FLAG) {
            boolean inverted = type.startsWith("!");
            String flagType = inverted ? type.substring(1) : type;
            Flag flag = ensure(activities.getFlag(flagType), "Flag &c'" + type + "'&r doesn't exist.");
            activator.getLogic().getFlags().add(new Flag.Stored(flag, parameters, inverted));
            sendPrefixed(ctx, "&{selection} &{nameFormatted}&r was successfully added.", Map.of( // TODO Hover for params
                    "selection", selection.asStart,
                    "nameFormatted", inky((inverted ? "&6&l!&r&a" : "&a") + flag.getName())
            ));
        } else {
            var target = selection == ActivitySelection.ACTION
                    ? activator.getLogic().getActions()
                    : activator.getLogic().getReactions();
            Action action = ensure(activities.getAction(type), "Action &c'" + type + "'&r doesn't exist.");
            target.add(new Action.Stored(action, parameters));
            sendPrefixed(ctx, "&{selection} &{name}&r was successfully added.", Map.of( // TODO Hover for params
                    "selection", selection.asStart,
                    "name", action.getName()
            ));
        }
        saveActivator(activator);
        return Command.SINGLE_SUCCESS;
    }

    private int activityRemove(@NotNull CommandContext<CommandSourceStack> ctx, ActivitySelection selection) {
        Activator activator = getActivator(ctx);
        List<? extends Activity.Stored<?>> list = getActivityList(activator, selection);
        int index = IntegerArgumentType.getInteger(ctx, "index");
        if (index < 0 || index >= list.size()) {
            sendPrefixed(ctx, "There's no &c" + selection + "&r under index &c" + index + "&r.");
            return Command.SINGLE_SUCCESS;
        }
        Activity.Stored<?> activity = list.remove(index);
        sendPrefixed(ctx, "Successfully removed &{type} &a&{name}&r at index &a&{index}&r.", Map.of( // TODO Hover for params
                "type", selection.lower,
                "name", activity.getActivity().getName(),
                "index", index
        ));
        saveActivator(activator);
        return Command.SINGLE_SUCCESS;
    }

    private int activityMove(@NotNull CommandContext<CommandSourceStack> ctx, ActivitySelection selection) {
        Activator activator = getActivator(ctx);
        activityMoveList(ctx, selection, getActivityList(activator, selection));
        saveActivator(activator);
        return Command.SINGLE_SUCCESS;
    }

    private <T extends Activity.Stored<?>> void activityMoveList(
            @NotNull CommandContext<CommandSourceStack> ctx,
            ActivitySelection selection,
            List<T> list
    ) {
        int from = IntegerArgumentType.getInteger(ctx, "from");
        if (from < 0 || from >= list.size()) {
            sendPrefixed(ctx, "There's no &c" + selection + "&r with index &c" + from + "&r.");
            return;
        }
        int to = IntegerArgumentType.getInteger(ctx, "to");
        if (to < 0 || to >= list.size()) {
            sendPrefixed(ctx, "There's no &c" + selection + "&r with index &c" + to + "&r.");
            return;
        }
        if (from == to) {
            sendPrefixed(ctx, "You can't move &c" + selection + "&r onto itself.");
            return;
        }
        if (to > from) to--;
        list.add(to, list.remove(from));
        sendPrefixed(ctx, "Successfully moved &a" + selection + "&r.");
    }

    private @NotNull List<? extends Activity.Stored<?>> getActivityList(@NotNull Activator activator, @NotNull ActivitySelection selection) {
        return switch (selection) {
            case FLAG -> activator.getLogic().getFlags();
            case ACTION -> activator.getLogic().getActions();
            case REACTION -> activator.getLogic().getReactions();
        };
    }

    private @NotNull Activator getActivator(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        return ensure(activators.getActivator(name), "Activator &c'" + esc(name) + "'&r doesn't exist!");
    }

    private void saveActivator(@NotNull Activator activator) {
        activators.saveGroup(activator.getLogic().getGroup());
    }

    private enum ActivitySelection {
        REACTION, ACTION, FLAG;

        private final String lower;
        private final String asStart;

        ActivitySelection() {
            this.lower = name().toLowerCase(Locale.ROOT);
            this.asStart = name().charAt(0) + lower.substring(1);
        }

        @Override
        public String toString() {
            return lower;
        }
    }
}