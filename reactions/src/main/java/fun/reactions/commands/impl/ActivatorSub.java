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
                if (isFlag && storedActivity instanceof Flag.Stored storedFlag) {
                    sender.sendMessage(
                            inky(" " + (i + 1) + (storedFlag.isInverted() ? "&c&l!&r " : " ") + "&e" + activity.getName() + " &7= &r")
                                    .append(text(storedFlag.getContent()))
                    );
                } else {
                    sender.sendMessage(inky(" " + (i + 1) + " &e" + activity.getName() + " &7= &r").append(text(storedActivity.getContent())));
                }
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
