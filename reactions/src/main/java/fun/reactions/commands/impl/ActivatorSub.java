package fun.reactions.commands.impl;

import fun.reactions.ReActions;
import fun.reactions.commands.RaCommand;
import fun.reactions.commands.nodes.Node;
import fun.reactions.commands.nodes.StringArgNode;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activity.ActivitiesRegistry;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.util.parameter.Parameters;
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

// TODO
public class ActivatorSub extends RaCommand {
    private final ActivatorsManager activators;
    private final ActivitiesRegistry activities;

    public ActivatorSub(@NotNull ReActions.Platform platform) {
        super(platform);
        this.activators = platform.getActivators();
        this.activities = platform.getActivities();
    }

    @Override
    public @NotNull Node asNode() {
        return literal("activator", stringArg("name", StringArgNode.Type.WORD, this::help,
                literal("info", this::info),
                literal("delete", this::delete, literal("confirm", this::delete)),
                activityNode(ActivityType.ACTION, activities::getActionsTypesNames),
                activityNode(ActivityType.REACTION, activities::getActionsTypesNames),
                activityNode(ActivityType.FLAG, activities::getFlagsTypesNames)
        ));
    }

    private @NotNull Node activityNode(ActivityType type, Supplier<Collection<String>> suggests) {
        return literal(type.name().toLowerCase(Locale.ROOT), (p, s) -> activityHelp(p, s, type),
                literal("add", stringArg("type", StringArgNode.Type.WORD, suggests, stringArg("args", StringArgNode.Type.OPTIONAL_GREEDY, (p, s) -> activityAdd(p, s, type)))),
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
        sender.sendMessage(text()
                .append(inky("&7" + logic.getGroup() + "/&6&l" + logic.getName()))
                .append(inky("&e (" + logic.getType() + ")")));
        List<Flag.Stored> flags = logic.getFlags();
        if (!flags.isEmpty()) {
            inky(sender, "&aFlags:");
            for (int i = 0; i < flags.size(); i++) {
                Flag.Stored flag = flags.get(i);
                Flag type = flag.getActivity();
                sender.sendMessage(
                        inky(" " + (i + 1) + (flag.isInverted() ? "&c&l!&r " : " ") + "&e" + type.getName() + " &7= &r")
                                .append(text(flag.getContent()))
                );
            }
        }
        List<Action.Stored> actions = logic.getActions();
        if (!actions.isEmpty()) {
            inky(sender, "&aActions:");
            for (int i = 0; i < actions.size(); i++) {
                Action.Stored action = actions.get(i);
                Action type = action.getActivity();
                sender.sendMessage(inky(" " + (i + 1) + " &e" + type.getName() + " &7= &r").append(text(action.getContent())));
            }
        }
        actions = logic.getReactions();
        if (!actions.isEmpty()) {
            inky(sender, "&aReactions:");
            for (int i = 0; i < actions.size(); i++) {
                Action.Stored action = actions.get(i);
                Action type = action.getActivity();
                sender.sendMessage(inky(" " + (i + 1) + " &e" + type.getName() + " &7= &r").append(text(action.getContent())));
            }
        }
    }

    private void delete(@NotNull Parameters params, @NotNull CommandSender sender) {
        Activator activator = getActivator(params);
        if (!params.getString("full-command").endsWith(" confirm")) {
            sender.sendMessage("Add confirm to the end of a command");
        } else {
            activators.removeActivator(activator.getLogic().getName());
            sender.sendMessage("Activator removed");
        }
    }

    private void activityHelp(@NotNull Parameters params, @NotNull CommandSender sender, ActivityType activityType) {
        Activator activator = getActivator(params);
        sender.sendMessage(activityType + " help");
    }

    private void activityAdd(@NotNull Parameters params, @NotNull CommandSender sender, ActivityType activityType) {
        Activator activator = getActivator(params);
        sender.sendMessage(activityType + " add");
    }

    private void activityRemove(@NotNull Parameters params, @NotNull CommandSender sender, ActivityType activityType) {
        Activator activator = getActivator(params);
        sender.sendMessage(activityType + " remove");
    }

    private void activityMove(@NotNull Parameters params, @NotNull CommandSender sender, ActivityType activityType) {
        Activator activator = getActivator(params);
        sender.sendMessage(activityType + " move");
    }

    private @NotNull Activator getActivator(Parameters params) {
        String name = params.getString("name");
        return ensurePrefixed(activators.getActivator(name), "Activator &c'" + escape(name) + "'&r doesn't exist!");
    }

    private enum ActivityType {
        REACTION, ACTION, FLAG
    }
}
