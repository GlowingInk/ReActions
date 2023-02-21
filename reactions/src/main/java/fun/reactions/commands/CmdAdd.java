package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.activity.actions.Action;
import fun.reactions.logic.activity.flags.Flag;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.message.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CmdDefine(command = "reactions", description = Msg.CMD_ADD, permission = "reactions.config",
        subCommands = {"add"}, allowConsole = true,
        shortDescription = "&3/react add <Id> [f <flag> <param> | <a|r> <action> <param>]")
public class CmdAdd extends Cmd {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 1) return false;
        Player player = (sender instanceof Player) ? (Player) sender : null;
        String arg1 = args[1];
        String arg2 = args.length >= 3 ? args[2] : "";
        String arg3 = args.length >= 4 ? args[3] : "";
        StringBuilder arg4 = new StringBuilder(args.length >= 5 ? args[4] : "");
        if (args.length > 5) {
            for (int i = 5; i < args.length; i++)
                arg4.append(" ").append(args[i]);
            arg4 = new StringBuilder(arg4.toString().trim());
        }
        if (ReActions.getActivators().containsActivator(arg1)) {
            String param = LocationUtils.parsePlaceholders(player, arg4.toString()); // используется в addActions
            Activator act = ReActions.getActivators().getActivator(arg1);
            switch (arg2) {
                case "a":
                case "action":
                    if (addAction(act, arg3, param)) {
                        Msg.CMD_ACTADDED.print(sender, arg3 + " (" + param + ")");
                        break;
                    } else {
                        Msg.CMD_ACTNOTADDED.print(sender, arg3 + " (" + param + ")");
                        return true;
                    }
                case "r":
                case "reaction":
                    if (addReaction(act, arg3, param)) {
                        Msg.CMD_REACTADDED.print(sender, arg3 + " (" + param + ")");
                        break;
                    } else {
                        Msg.CMD_REACTNOTADDED.print(sender, arg3 + " (" + param + ")");
                        return true;
                    }
                case "f":
                case "flag":
                    if (addFlag(act, arg3, param)) {
                        Msg.CMD_FLAGADDED.print(sender, arg3 + " (" + param + ")");
                        break;
                    } else {
                        Msg.CMD_FLAGNOTADDED.print(sender, arg3 + " (" + param + ")");
                        return true;
                    }
                default:
                    Msg.CMD_UNKNOWNBUTTON.print(sender, arg2);
                    return true;
            }
            ReActions.getActivators().saveGroup(act.getLogic().getGroup());
        } else {
            Msg.CMD_UNKNOWNADD.print(sender, 'c');
        }
        return true;
    }

    private boolean addAction(Activator activator, String act, String param) {
        Action action = ReActions.getActivities().getAction(act);
        if (action != null) {
            activator.getLogic().addAction(action, param);
            return true;
        }
        return false;
    }

    private boolean addReaction(Activator activator, String act, String param) {
        Action action = ReActions.getActivities().getAction(act);
        if (action != null) {
            activator.getLogic().addReaction(action, param);
            return true;
        }
        return false;
    }

    private boolean addFlag(Activator activator, String fl, String param) {
        boolean invert = fl.startsWith("!");
        Flag flag = ReActions.getActivities().getFlag(invert ? fl.substring(1) : fl);
        if (flag != null) {
            activator.getLogic().addFlag(flag, param, invert);
            return true;
        }
        return false;
    }

}
