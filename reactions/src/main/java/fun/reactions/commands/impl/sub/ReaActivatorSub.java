package fun.reactions.commands.impl.sub;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
import fun.reactions.commands.RegistryArgument;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static fun.reactions.commands.RegistryArgument.registryArgument;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;

@SuppressWarnings("SameReturnValue")
public final class ReaActivatorSub extends RaCommandBase {
    private static final DynamicCommandExceptionType UNKNOWN_ACTIVATOR = new DynamicCommandExceptionType(
            name -> new LiteralMessage("Activator '" + name + "' doesn't exist!")
    );

    private final ActivatorsManager activators;
    private final ActivitiesRegistry activities;
    private final ActivatorTypesRegistry types;

    public ReaActivatorSub(@NotNull ReActions.Platform platform) {
        super(platform);
        this.activators = platform.getActivators();
        this.activities = platform.getActivities();
        this.types = platform.getActivatorTypes();
    }

    @Override
    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        var actionArg = registryArgument(activities::getAction, activities::getActionsTypesNames);

        return literal("activator")
                .requires(permission("reactions.activator"))
                .executes(ctx -> promptForName(ctx, "activator"))
                .then(argument("name", StringArgumentType.word())
                        .suggests((_, builder) -> {
                            String remaining = builder.getRemaining();
                            activators.getActivatorNames().stream()
                                    .filter(s -> s.startsWith(remaining))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(this::help)
                        .then(literal("create")
                                .requires(permission("reactions.activator.edit"))
                                .then(argument("type", registryArgument(types::get, types::getTypeNames))
                                        .executes(ctx -> create(ctx, ""))
                                        .then(argument("parameters", StringArgumentType.greedyString())
                                                .executes(ctx -> create(ctx, StringArgumentType.getString(ctx, "parameters"))))))
                        .then(literal("info")
                                .requires(permission("reactions.activator.view"))
                                .executes(this::info))
                        .then(literal("delete")
                                .requires(permission("reactions.activator.edit"))
                                .executes(this::deletePrompt)
                                .then(literal("confirm").executes(this::delete)))
                        .then(actionActivityNode(ActivitySelection.ACTION, actionArg))
                        .then(actionActivityNode(ActivitySelection.REACTION, actionArg))
                        .then(flagActivityNode()))
                .build();
    }

    private @NotNull LiteralCommandNode<CommandSourceStack> actionActivityNode(
            @NotNull ActivitySelection selection,
            @NotNull RegistryArgument<Action> actionArg
    ) {
        var node = literal(selection.lower)
                .executes(ctx -> activityHelp(ctx, selection))
                .then(literal("add")
                        .requires(permission("reactions.activator.edit"))
                        .then(argument("type", actionArg)
                                .executes(ctx -> actionAdd(ctx, selection, ""))
                                .then(argument("parameters", StringArgumentType.greedyString())
                                        .executes(ctx -> actionAdd(ctx, selection, StringArgumentType.getString(ctx, "parameters"))))));
        return appendRemoveMoveNodes(node, selection).build();
    }

    private @NotNull LiteralCommandNode<CommandSourceStack> flagActivityNode() {
        var node = literal(ActivitySelection.FLAG.lower)
                .executes(ctx -> activityHelp(ctx, ActivitySelection.FLAG))
                .then(literal("add")
                        .requires(permission("reactions.activator.edit"))
                        .then(argument("type", StringArgumentType.word())
                                .suggests((_, builder) -> {
                                    String remaining = builder.getRemaining();
                                    activities.getFlagsTypesNames().stream()
                                            .filter(s -> s.startsWith(remaining))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> flagAdd(ctx, ""))
                                .then(argument("parameters", StringArgumentType.greedyString())
                                        .executes(ctx -> flagAdd(ctx, StringArgumentType.getString(ctx, "parameters"))))));
        return appendRemoveMoveNodes(node, ActivitySelection.FLAG).build();
    }

    private @NotNull LiteralArgumentBuilder<CommandSourceStack> appendRemoveMoveNodes(
            @NotNull LiteralArgumentBuilder<CommandSourceStack> builder,
            @NotNull ActivitySelection selection
    ) {
        return builder
                .then(literal("remove")
                        .requires(permission("reactions.activator.edit"))
                        .then(argument("index", IntegerArgumentType.integer(1))
                                .executes(ctx -> activityRemove(ctx, selection))))
                .then(literal("move")
                        .requires(permission("reactions.activator.edit"))
                        .then(argument("from", IntegerArgumentType.integer(1))
                                .then(argument("to", IntegerArgumentType.integer(1))
                                        .executes(ctx -> activityMove(ctx, selection)))));
    }

    private int help(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = ctx.getArgument("name", String.class);
        if (activators.getActivator(name) == null) {
            return suggestCreate(ctx, "activator " + name, "Activator", name, "create");
        }
        return sendHelp(ctx, "activator " + name,
                "create", "&a<type> &e[<parameters...>]", "Create this activator with the given &atype&r and &eparameters",
                "info", "", "Show info about this activator",
                "move", "&a<group>", "Move this activator into another group",
                "delete", "[confirm]", "Delete this activator",
                "action", "...", "Manage activator actions",
                "reaction", "...", "Manage activator reactions",
                "flag", "...", "Manage activator flags"
        );
    }

    private int create(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String rawParameters) {
        String name = StringArgumentType.getString(ctx, "name");
        if (activators.getActivator(name) != null) {
            sendPrefixed(ctx, "Activator &c'" + esc(name) + "'&r already exists.");
            return SINGLE_SUCCESS;
        }
        ActivatorType type = ctx.getArgument("type", ActivatorType.class);
        Activator activator = type.createActivator(
                new Logic(platform, type.getName(), name),
                Parameters.fromString(rawParameters)
        );
        if (activator == null) {
            sendPrefixed(ctx, "Failed to create activator&c!");
            return SINGLE_SUCCESS;
        }
        activators.addActivator(activator, true);
        sendPrefixed(ctx, "Activator &a'&{name}'&r of type &a'&{type}'&r was created.", Map.of(
                "name", activator.getLogic().getName(),
                "type", activator.getLogic().getType()
        ));
        return SINGLE_SUCCESS;
    }

    private int info(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Activator activator = getActivator(ctx);
        Logic logic = activator.getLogic();
        CommandSender sender = ctx.getSource().getSender();
        sender.sendMessage("");
        sender.sendMessage(text()
                .append(inky("&7" + logic.getGroup() + "/&6&l" + logic.getName()))
                .append(inky("&e (" + logic.getType() + ")")));
        sendActivityInfo(sender, logic.getFlags(), "&aFlags:", true);
        sendActivityInfo(sender, logic.getActions(), "&aActions:", false);
        sendActivityInfo(sender, logic.getReactions(), "&aReactions:", false);
        return SINGLE_SUCCESS;
    }

    private void sendActivityInfo(
            @NotNull CommandSender sender,
            @NotNull List<? extends Activity.Stored<?>> storeds,
            @NotNull String title,
            boolean isFlag
    ) {
        if (storeds.isEmpty()) return;
        sendInky(sender, title);
        for (int i = 0; i < storeds.size(); i++) {
            Activity.Stored<?> stored = storeds.get(i);
            Activity activity = stored.getActivity();
            Component prefix = (isFlag && stored instanceof Flag.Stored storedFlag)
                    ? inky(" &7" + (i + 1) + (storedFlag.isInverted() ? " &c&l!&r" : " "))
                    : inky(" &7" + (i + 1) + " ");
            sender.sendMessage(prefix
                    .append(inky("&e" + activity.getName() + " &7= &r"))
                    .append(text(stored.getContent())));
        }
    }

    private int deletePrompt(@NotNull CommandContext<CommandSourceStack> ctx) {
        sendPrefixed(ctx, "Append &econfirm&r to the command to proceed.");
        return SINGLE_SUCCESS;
    }

    private int delete(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Activator activator = getActivator(ctx);
        String name = activator.getLogic().getName();
        activators.removeActivator(name);
        saveActivator(activator);
        sendPrefixed(ctx, "Activator &a'&{name}'&r was successfully removed.", Map.of("name", name));
        return SINGLE_SUCCESS;
    }

    private int activityHelp(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull ActivitySelection selection) {
        return sendHelp(ctx, "activator " + esc(ctx.getArgument("name", String.class)) + " " + selection,
                "add", "<type> &e[parameters...]", "Add a &a" + selection + "&r to the activator",
                "remove", "<index>", "Remove the &a" + selection + "&r at the given index",
                "move", "<from> <to>", "Move a &a" + selection + "&r to another index"
        );
    }

    private int actionAdd(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull ActivitySelection selection,
            @NotNull String parameters
    ) throws CommandSyntaxException {
        Activator activator = getActivator(ctx);
        Action action = ctx.getArgument("type", Action.class);
        var target = selection == ActivitySelection.ACTION
                ? activator.getLogic().getActions()
                : activator.getLogic().getReactions();
        target.add(new Action.Stored(action, parameters));
        saveActivator(activator);
        sendPrefixed(ctx, "&{selection} &a&{name}&r was successfully added.", Map.of(
                "selection", selection.asStart,
                "name", action.getName()
        ));
        return SINGLE_SUCCESS;
    }

    private int flagAdd(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull String parameters
    ) throws CommandSyntaxException {
        Activator activator = getActivator(ctx);
        String rawType = StringArgumentType.getString(ctx, "type");
        boolean inverted = rawType.startsWith("!");
        String flagType = inverted ? rawType.substring(1) : rawType;
        Flag flag = activities.getFlag(flagType);
        if (flag == null) {
            sendPrefixed(ctx, "Flag &c'" + esc(flagType) + "'&r doesn't exist.");
            return SINGLE_SUCCESS;
        }
        activator.getLogic().getFlags().add(new Flag.Stored(flag, parameters, inverted));
        saveActivator(activator);
        sendPrefixed(ctx, "&{selection} &{nameFormatted}&r was successfully added.", Map.of(
                "selection", ActivitySelection.FLAG.asStart,
                "nameFormatted", inky((inverted ? "&6&l!&r&a" : "&a") + flag.getName())
        ));
        return SINGLE_SUCCESS;
    }

    private int activityRemove(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull ActivitySelection selection
    ) throws CommandSyntaxException {
        Activator activator = getActivator(ctx);
        List<? extends Activity.Stored<?>> list = getActivityList(activator, selection);
        int index = IntegerArgumentType.getInteger(ctx, "index");
        if (index > list.size()) {
            sendPrefixed(ctx, "There's no &c" + selection + "&r at index &c" + index + "&r.");
            return SINGLE_SUCCESS;
        }
        Activity.Stored<?> removed = list.remove(index - 1);
        saveActivator(activator);
        sendPrefixed(ctx, "Removed &a" + selection + "&r &a'&{name}'&r at index &a" + index + "&r.", Map.of(
                "name", removed.getActivity().getName()
        ));
        return SINGLE_SUCCESS;
    }

    private int activityMove(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull ActivitySelection selection
    ) throws CommandSyntaxException {
        Activator activator = getActivator(ctx);
        int from = IntegerArgumentType.getInteger(ctx, "from");
        int to = IntegerArgumentType.getInteger(ctx, "to");
        doMove(getActivityList(activator, selection), selection, ctx, from, to);
        saveActivator(activator);
        return SINGLE_SUCCESS;
    }

    /**
     * Generic helper so the wildcard in {@code List<? extends Activity.Stored<?>>} is
     * captured as {@code T}, letting us pass the result of {@code remove} back to {@code add}.
     */
    private <T extends Activity.Stored<?>> void doMove(
            @NotNull List<T> list,
            @NotNull ActivitySelection selection,
            @NotNull CommandContext<CommandSourceStack> ctx,
            int from, int to
    ) {
        if (from > list.size()) {
            sendPrefixed(ctx, "There's no &c" + selection + "&r at index &c" + from + "&r.");
            return;
        }
        if (to > list.size()) {
            sendPrefixed(ctx, "There's no &c" + selection + "&r at index &c" + to + "&r.");
            return;
        }
        if (from == to) {
            sendPrefixed(ctx, "Cannot move &c" + selection + "&r onto itself.");
            return;
        }
        int fromIdx = from - 1;
        int toIdx = to - 1;
        if (toIdx > fromIdx) toIdx--;
        list.add(toIdx, list.remove(fromIdx));
        sendPrefixed(ctx, "Moved &a" + selection + "&r from index &a" + from + "&r to &a" + to + "&r.");
    }

    private @NotNull List<? extends Activity.Stored<?>> getActivityList(
            @NotNull Activator activator,
            @NotNull ActivitySelection selection
    ) {
        return switch (selection) {
            case FLAG -> activator.getLogic().getFlags();
            case ACTION -> activator.getLogic().getActions();
            case REACTION -> activator.getLogic().getReactions();
        };
    }

    /**
     * Resolves the activator by name or throws a {@link CommandSyntaxException}.
     */
    private @NotNull Activator getActivator(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        Activator activator = activators.getActivator(name);
        if (activator == null) throw UNKNOWN_ACTIVATOR.create(name);
        return activator;
    }

    private void saveActivator(@NotNull Activator activator) {
        activators.saveGroup(activator.getLogic().getGroup());
    }

    private enum ActivitySelection {
        REACTION, ACTION, FLAG;

        final String lower;
        final String asStart;

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