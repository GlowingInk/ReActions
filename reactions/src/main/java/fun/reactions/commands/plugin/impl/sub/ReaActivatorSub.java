package fun.reactions.commands.plugin.impl.sub;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.plugin.RaCommandBase;
import fun.reactions.commands.plugin.RegistryArgument;
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
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static fun.reactions.commands.plugin.RegistryArgument.registryArgument;
import static fun.reactions.util.Utils.upperFirst;
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
                        .suggests(suggestNames(activators::getActivatorNames, false))
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
                        .then(actionNode(ActivitySelection.ACTION, actionArg))
                        .then(actionNode(ActivitySelection.REACTION, actionArg))
                        .then(flagNode()))
                .build();
    }

    private @NotNull LiteralCommandNode<CommandSourceStack> actionNode(
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
        return appendActivityNodes(node, selection).build();
    }

    private @NotNull LiteralCommandNode<CommandSourceStack> flagNode() {
        var node = literal(ActivitySelection.FLAG.lower)
                .executes(ctx -> activityHelp(ctx, ActivitySelection.FLAG))
                .then(literal("add")
                        .requires(permission("reactions.activator.edit"))
                        .then(argument("type", StringArgumentType.string())
                                .suggests(suggestNames(activities::getFlagsTypesNames, false))
                                .executes(ctx -> flagAdd(ctx, ""))
                                .then(argument("parameters", StringArgumentType.greedyString())
                                        .executes(ctx -> flagAdd(ctx, StringArgumentType.getString(ctx, "parameters"))))))
                .then(literal("invert")
                        .requires(permission("reactions.activator.edit"))
                        .then(argument("index", IntegerArgumentType.integer(1))
                                .executes(this::flagInvert)));
        return appendActivityNodes(node, ActivitySelection.FLAG).build();
    }

    private @NotNull LiteralArgumentBuilder<CommandSourceStack> appendActivityNodes(
            @NotNull LiteralArgumentBuilder<CommandSourceStack> builder,
            @NotNull ActivitySelection selection
    ) {
        return builder
                .then(literal("edit")
                        .requires(permission("reactions.activator.view"))
                        .then(argument("index", IntegerArgumentType.integer(1))
                                .executes(ctx -> activityEdit(ctx, selection))))
                .then(literal("change")
                        .requires(permission("reactions.activator.edit"))
                        .then(argument("index", IntegerArgumentType.integer(1))
                                .executes(ctx -> activityChange(ctx, selection, ""))
                                .then(argument("parameters", StringArgumentType.greedyString())
                                        .executes(ctx -> activityChange(ctx, selection, StringArgumentType.getString(ctx, "parameters"))))))
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
        return sendHelp(ctx, "activator", name, activators.getActivator(name) != null, "create",
                "create", "&a<type> &e[<parameters...>]", "Create this activator with the given&a type&r and&e parameters",
                "info", "", "Show info about this activator",
                "move", "&a<group>", "Move this activator into another&a group",
                "delete", "[confirm]", "Delete this activator",
                "action", "...", "Manage activator actions",
                "reaction", "...", "Manage activator reactions",
                "flag", "...", "Manage activator flags"
        );
    }

    private int create(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String rawParameters) {
        String name = StringArgumentType.getString(ctx, "name");
        if (activators.getActivator(name) != null) {
            sendAlreadyExists(ctx, "Activator", name);
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

        MemoryConfiguration infoCfg = new MemoryConfiguration();
        activator.saveOptions(infoCfg);
        var keys = infoCfg.getKeys(false);
        if (!keys.isEmpty()) {
            sendInky(sender, "&aOptions:");
            for (String optionKey : keys) {
                var optionValue = infoCfg.get(optionKey);
                if (optionValue == null) continue;
                if (optionValue instanceof Collection<?> coll) {
                    if (coll.isEmpty()) {
                        sendInky(sender, " &e" + esc(optionKey) + " &7= &r[ ]");
                    } else {
                        sendInky(sender, " &e" + esc(optionKey) + " &7= &r[");
                        for (Object subValue : coll) {
                            sender.sendMessage("  " + subValue);
                        }
                        sender.sendMessage(" ]");
                    }
                } else {
                    sendInky(sender, " &e" + esc(optionKey) + " &7= &r" + esc(optionValue.toString()));
                }
            }
        }

        sendActivityInfo(ctx, activator, ActivitySelection.FLAG);
        sendActivityInfo(ctx, activator, ActivitySelection.ACTION);
        sendActivityInfo(ctx, activator, ActivitySelection.REACTION);
        return SINGLE_SUCCESS;
    }

    private void sendActivityInfo(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull Activator activator,
            @NotNull ActivitySelection selection
    ) {
        var storeds = getActivityList(activator, selection);
        if (storeds.isEmpty()) return;
        CommandSender sender = ctx.getSource().getSender();
        sendInky(sender, "&a" + upperFirst(selection.name()) + ":");
        boolean isFlag = selection == ActivitySelection.FLAG;
        String base = sender instanceof Player
                ? "/" + rootLabel(ctx) + " activator " + activator.getLogic().getName() + " " + selection.lower
                : null;
        for (int i = 0; i < storeds.size(); i++) {
            int index = i + 1;
            Activity.Stored<?> stored = storeds.get(i);
            Activity activity = stored.getActivity();
            Component prefix = (isFlag && stored instanceof Flag.Stored storedFlag)
                    ? inky(" &7" + index + (storedFlag.isInverted() ? " &c&l!&r" : " "))
                    : inky(" &7" + index + " ");
            Component name = base != null
                    ? inky("&[&e" + activity.getName() + "](click:run " + base + " edit " + index + ")(hover:text &7Click to edit)")
                    : inky("&e" + activity.getName());
            sender.sendMessage(prefix.append(name).append(inky(" &7= &r")).append(text(stored.getContent())));
        }
    }

    private int activityEdit(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull ActivitySelection selection
    ) throws CommandSyntaxException {
        Activator activator = getActivator(ctx);
        var activitiesList = getActivityList(activator, selection);
        int index = IntegerArgumentType.getInteger(ctx, "index");
        if (index > activitiesList.size()) {
            sendPrefixed(ctx, "There's no &c" + selection + "&r at index &c" + index + "&r.");
            return SINGLE_SUCCESS;
        }
        Activity.Stored<?> stored = activitiesList.get(index - 1);
        CommandSender sender = ctx.getSource().getSender();
        sender.sendMessage("");
        Component header = inky("&a" + upperFirst(selection.name()) + " &7#" + index + " &e" + stored.getActivity().getName());
        if (stored instanceof Flag.Stored flagStored && flagStored.isInverted()) {
            header = header.append(inky(" &c&l!"));
        }
        sender.sendMessage(header);
        sender.sendMessage(text("  ").append(text(stored.getContent())));
        if (sender instanceof Player) {
            sender.sendMessage("");
            String base = "/" + rootLabel(ctx) + " activator " + activator.getLogic().getName() + " " + selection.lower;
            sendInky(sender, button("&a", "Change value", "suggest", base + " change " + index + " ", "Edit the value"));
            if (index > 1) {
                sendInky(sender, button("&e", "↑ Move up", "run", base + " move " + index + " " + (index - 1), "Move up one slot"));
            }
            if (index < activitiesList.size()) {
                sendInky(sender, button("&e", "↓ Move down", "run", base + " move " + index + " " + (index + 1), "Move down one slot"));
            }
            if (stored instanceof Flag.Stored flagStored) {
                sendInky(sender, flagStored.isInverted()
                        ? button("&a", "✔ Inverted", "run", base + " invert " + index, "Click to un-invert")
                        : button("&7", "❌ Not inverted", "run", base + " invert " + index, "Click to invert"));
            }
            sendInky(sender, button("&c", "❌ Delete", "run", base + " remove " + index, "Delete this " + selection));
        }
        return SINGLE_SUCCESS;
    }

    private static @NotNull String button(
            @NotNull String color,
            @NotNull String label,
            @NotNull String clickType,
            @NotNull String command,
            @NotNull String hover
    ) {
        return "  &7[" + color + label + "&7](click:" + clickType + " " + command + ")(hover:text &7" + hover + ")";
    }

    private int deletePrompt(@NotNull CommandContext<CommandSourceStack> ctx) {
        sendPrefixed(ctx, "Append&e 'confirm'&r to the command to proceed.");
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
        List<String> help = new ArrayList<>(List.of(
                "add", "&a<type> &e[<parameters...>]", "Add a &a" + selection + "&r to the activator",
                "edit", "&a<index>", "Show the &a" + selection + "&r at the given index, with edit buttons",
                "change", "&a<index> &e[<parameters...>]", "Change the parameters of the &a" + selection + "&r at the given index",
                "remove", "&a<index>", "Remove the &a" + selection + "&r at the given index",
                "move", "&a<from> <to>", "Move a &a" + selection + "&r to another index"
        ));
        if (selection == ActivitySelection.FLAG) {
            help.addAll(List.of("invert", "&a<index>", "Toggle inversion of the&a flag&r at the given index"));
        }
        return sendHelp(ctx, "activator " + esc(ctx.getArgument("name", String.class)) + " " + selection, help.toArray(new String[0]));
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

    private int activityChange(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull ActivitySelection selection,
            @NotNull String parameters
    ) throws CommandSyntaxException {
        Activator activator = getActivator(ctx);
        List<? extends Activity.Stored<?>> list = getActivityList(activator, selection);
        int index = IntegerArgumentType.getInteger(ctx, "index");
        if (index > list.size()) {
            sendPrefixed(ctx, "There's no &c" + selection + "&r at index &c" + index + "&r.");
            return SINGLE_SUCCESS;
        }
        Activity.Stored<?> stored = list.get(index - 1);
        Activity.Stored<?> updated = stored instanceof Flag.Stored flagStored
                ? new Flag.Stored(flagStored.getActivity(), parameters, flagStored.isInverted())
                : new Action.Stored((Action) stored.getActivity(), parameters);
        setAt(list, index - 1, updated);
        saveActivator(activator);
        sendPrefixed(ctx, "Changed &a" + selection + "&r &a'" + updated.getActivity().getName() + "'&r at index &a" + index + "&r.");
        return SINGLE_SUCCESS;
    }

    private int flagInvert(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Activator activator = getActivator(ctx);
        List<Flag.Stored> list = activator.getLogic().getFlags();
        int index = IntegerArgumentType.getInteger(ctx, "index");
        if (index > list.size()) {
            sendPrefixed(ctx, "There's no &cflag&r at index &c" + index + "&r.");
            return SINGLE_SUCCESS;
        }
        Flag.Stored stored = list.get(index - 1);
        Flag.Stored updated = new Flag.Stored(stored.getActivity(), stored.getContent(), !stored.isInverted());
        list.set(index - 1, updated);
        saveActivator(activator);
        sendPrefixed(ctx, "&{state} &aflag&r &a'&{name}'&r at index &a" + index + "&r.", Map.of(
                "state", updated.isInverted() ? "Inverted" : "Un-inverted",
                "name", updated.getActivity().getName()
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
        List<? extends Activity.Stored<?>> list = getActivityList(activator, selection);
        int from = IntegerArgumentType.getInteger(ctx, "from");
        int to = IntegerArgumentType.getInteger(ctx, "to");
        if (from > list.size()) {
            sendPrefixed(ctx, "There's no &c" + selection + "&r at index &c" + from + "&r.");
            return SINGLE_SUCCESS;
        }
        if (to > list.size()) {
            sendPrefixed(ctx, "There's no &c" + selection + "&r at index &c" + to + "&r.");
            return SINGLE_SUCCESS;
        }
        if (from == to) {
            sendPrefixed(ctx, "Cannot move &c" + selection + "&r onto itself.");
            return SINGLE_SUCCESS;
        }
        moveAt(list, from - 1, to - 1);
        saveActivator(activator);
        sendPrefixed(ctx, "Moved &a" + selection + "&r from index &a" + from + "&r to &a" + to + "&r.");
        return SINGLE_SUCCESS;
    }

    @SuppressWarnings("unchecked") // eh, whatever
    private static <T extends Activity.Stored<?>> void setAt(@NotNull List<T> list, int index, @NotNull Activity.Stored<?> value) {
        list.set(index, (T) value);
    }

    private static <T extends Activity.Stored<?>> void moveAt(@NotNull List<T> list, int fromIdx, int toIdx) {
        list.add(toIdx, list.remove(fromIdx));
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