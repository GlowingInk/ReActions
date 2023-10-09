package fun.reactions.commands.impl;

import fun.reactions.ReActions;
import fun.reactions.commands.RaCommand;
import fun.reactions.commands.nodes.Node;
import fun.reactions.commands.nodes.StringArgNode;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activity.ActivitiesRegistry;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.util.parameter.Parameters;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static fun.reactions.commands.nodes.IntegerArgNode.integerArg;
import static fun.reactions.commands.nodes.LiteralNode.literal;
import static fun.reactions.commands.nodes.StringArgNode.stringArg;
import static net.kyori.adventure.text.Component.text;

public class ReaActivatorSub extends RaCommand {
    private final ActivatorsManager activators;
    private final ActivitiesRegistry activities;

    public ReaActivatorSub(@NotNull ReActions.Platform platform) {
        super(platform);
        this.activators = platform.getActivators();
        this.activities = platform.getActivities();
    }

    @Override
    public @NotNull Node asNode() {
        return literal("activator", stringArg("name", StringArgNode.Type.WORD, this::help,
                literal("info", this::info),
                literal("delete", this::delete, literal("confirm", this::delete)),
                activityNode(ActivitySelection.ACTION, activities::getActionsTypesNames),
                activityNode(ActivitySelection.REACTION, activities::getActionsTypesNames),
                activityNode(ActivitySelection.FLAG, activities::getFlagsTypesNames)
        ));
    }

    private @NotNull Node activityNode(ActivitySelection type, Supplier<Collection<String>> suggests) {
        return literal(type.name().toLowerCase(Locale.ROOT), (p, s) -> activityHelp(p, s, type),
                literal("add", stringArg("type", StringArgNode.Type.WORD, suggests, (p, s) -> activityAdd(p, s, type), stringArg("parameters", StringArgNode.Type.OPTIONAL_GREEDY))),
                literal("remove", integerArg("index", 1, (p, s) -> activityRemove(p, s, type))),
                literal("move", integerArg("from", 1, integerArg("to", 1, (p, s) -> activityMove(p, s, type))))
        );
    }

    private void help(@NotNull Parameters params, @NotNull CommandSender sender) {
        Activator activator = getActivator(params);
        sendHelp(sender, params, "activator " + activator.getLogic().getName(),
                "info", "", "Get info about an activator",
                "move", "&a<group>", "Move activator into another group",
                "delete", "[confirm]", "Delete an activator",
                "action", "...", "Manage activator actions",
                "reaction", "...", "Manage activator reactions",
                "flag", "...", "Manage activator flags"
        );
    }

    private void info(@NotNull Parameters params, @NotNull CommandSender sender) {
        Activator activator = getActivator(params);
        Logic logic = activator.getLogic();
        sender.sendMessage("");
        sender.sendMessage(text()
                .append(inky("&7" + logic.getGroup() + "/&6&l" + logic.getName()))
                .append(inky("&e (" + logic.getType() + ")")));
        sendActionsInfo(sender, logic.getFlags(), "&aFlags:", true);
        sendActionsInfo(sender, logic.getActions(), "&aActions:", false);
        sendActionsInfo(sender, logic.getReactions(), "&aReactions:", false);
    }

    private void sendActionsInfo(CommandSender sender, List<? extends Activity.Stored<?>> storeds, String title, boolean isFlag) {
        if (!storeds.isEmpty()) {
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
                sender.sendMessage(text.append(inky("&e" + activity.getName() + " &7= &r")).append(text(storedActivity.getContent())));
            }
        }
    }

    private void delete(@NotNull Parameters params, @NotNull CommandSender sender) {
        Activator activator = getActivator(params);
        if (!params.getString("full-command").endsWith(" confirm")) {
            sendPrefixed(sender, "Add confirm to the end of a command.");
        } else {
            String activatorName = activator.getLogic().getName();
            activators.removeActivator(activatorName);
            sendPrefixed(sender, "Activator &a'" + escape(activatorName) + "'&r was successfully removed.");
            saveActivator(activator);
        }
    }

    private void activityHelp(@NotNull Parameters params, @NotNull CommandSender sender, ActivitySelection selection) {
        Activator activator = getActivator(params);
        sendHelp(sender, params, "activator " + escape(activator.getLogic().getName()) + " " + selection,
                "add", "<type> [parameters...]", "Add &e" + selection + "&r to an activator",
                "remove", "<index>", "Remove &e" + selection + "&r from an activator",
                "move", "<from> <to>", "Move &e" + selection + "&r to another index"
        );
    }

    private void activityAdd(@NotNull Parameters params, @NotNull CommandSender sender, ActivitySelection selection) {
        Activator activator = getActivator(params);
        if (selection == ActivitySelection.FLAG) {
            String type = params.getString("type");
            boolean inverted = type.startsWith("!");
            activator.getLogic().getFlags().add(
                    new Flag.Stored(
                            ensure(activities.getFlag(inverted ? type.substring(1) : type), "Flag &c'" + type + "'&r doesn't exist."),
                            params.getString("parameters"),
                            inverted
                    )
            );
            sendPrefixed(sender, "&a" + selection.asStart() + " was successfully added."); // TODO
        } else {
            (selection == ActivitySelection.ACTION ? activator.getLogic().getActions() : activator.getLogic().getReactions()).add(
                    new Action.Stored(
                            ensure(activities.getAction(params.getString("type")), "Action &c'" + params.getString("type") + "'&r doesn't exist."),
                            params.getString("parameters")
                    )
            );
            sendPrefixed(sender, "&a" + selection.asStart() + " was successfully added."); // TODO
        }
        saveActivator(activator);
    }

    private void activityRemove(@NotNull Parameters params, @NotNull CommandSender sender, ActivitySelection selection) {
        Activator activator = getActivator(params);
        List<? extends Activity.Stored<?>> activities = switch (selection) {
            case FLAG -> activator.getLogic().getFlags();
            case ACTION -> activator.getLogic().getActions();
            case REACTION -> activator.getLogic().getReactions();
        };
        int index = params.getInteger("index");
        if (index < 0 || index >= activities.size()) {
            exception("There's no &c" + selection + "&r under index &c" + index + "&r.");
            return;
        }
        activities.remove(index);
        sendPrefixed(sender, "Successfully removed &a" + selection + "&r."); // TODO
        saveActivator(activator);
    }

    private void activityMove(@NotNull Parameters params, @NotNull CommandSender sender, ActivitySelection selection) {
        Activator activator = getActivator(params);
        List<? extends Activity.Stored<?>> activities = switch (selection) {
            case FLAG -> activator.getLogic().getFlags();
            case ACTION -> activator.getLogic().getActions();
            case REACTION -> activator.getLogic().getReactions();
        };
        activityMove(params, sender, selection, activities);
        saveActivator(activator);
    }

    private <T extends Activity.Stored<?>> void activityMove(@NotNull Parameters params, @NotNull CommandSender sender, ActivitySelection selection, List<T> activities) {
        int from = params.getInteger("from");
        if (from < 0 || from >= activities.size()) {
            exception("There's no &c" + selection + "&r under index &c" + from + "&r.");
            return;
        }
        int to = params.getInteger("to");
        if (to < 0 || to >= activities.size()) {
            exception("There's no &c" + selection + "&r under index &c" + to + "&r.");
            return;
        }
        if (from == to) {
            exception("You can't move &c" + selection + "&r onto itself.");
            return;
        }
        if (to > from) to--;
        var activity = activities.remove(from);
        activities.add(to, activity);
        sendPrefixed(sender, "Successfully moved &a" + selection + "&r."); // TODO
    }

    private @NotNull Activator getActivator(Parameters params) {
        String name = params.getString("name");
        return ensure(activators.getActivator(name), "Activator &c'" + escape(name) + "'&r doesn't exist!");
    }

    private void saveActivator(@NotNull Activator activator) {
        activators.saveGroup(activator.getLogic().getGroup());
    }

    private enum ActivitySelection {
        REACTION, ACTION, FLAG;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String asStart() {
            return switch (this) {
                case REACTION -> "Reaction";
                case ACTION -> "Action";
                case FLAG -> "Flag";
            };
        }
    }
}
