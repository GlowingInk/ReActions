package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.holders.LocationHolder;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.Logic;
import fun.reactions.time.timers.TimersManager;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.NumberUtils.Is;
import fun.reactions.util.message.Msg;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CmdDefine(command = "reactions", description = Msg.CMD_REMOVE, permission = "reactions.config",
        subCommands = {"remove|rmv|del|delete"}, allowConsole = true, shortDescription = "&3/react remove [loc|activator] <id>")
public class CmdRemove extends Cmd {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 1) return false;
        String arg1 = args[1];
        String arg2 = args.length >= 3 ? args[2] : "";
        StringBuilder arg3 = new StringBuilder(args.length >= 4 ? args[3] : "");
        if (args.length > 5) {
            for (int i = 4; i < args.length; i++)
                arg3.append(" ").append(args[i]);
            arg3 = new StringBuilder(arg3.toString().trim());
        }
        if (arg2.isEmpty()) return false;
        if (arg1.equalsIgnoreCase("act") || arg1.equalsIgnoreCase("activator")) {
            if (ReActions.getActivators().containsActivator(arg2)) {
                ReActions.getActivators().removeActivator(arg2);
                Msg.printMSG(sender, "msg_removebok", arg2);
            } else Msg.printMSG(sender, "msg_removebnf", arg2);
        } else if (arg1.equalsIgnoreCase("loc")) {
            if (LocationHolder.removeTpLoc(arg2)) {
                Msg.printMSG(sender, "msg_removelocok", arg2);
                LocationHolder.saveLocs();
            } else Msg.printMSG(sender, "msg_removelocnf", arg2);
        } else if (arg1.equalsIgnoreCase("timer") || arg1.equalsIgnoreCase("tmr")) {
            TimersManager.removeTimer(sender, arg2);
        } else if (arg1.equalsIgnoreCase("var") || arg1.equalsIgnoreCase("variable") || arg1.equalsIgnoreCase("variables")) {
            removeVariable(sender, arg2 + ((arg3.length() == 0) ? "" : " " + arg3));
        } else if (arg1.equalsIgnoreCase("menu") || arg1.equalsIgnoreCase("m")) {
            if (InventoryMenu.remove(arg2)) Msg.printMSG(sender, "msg_removemenu", arg2);
            else Msg.printMSG(sender, "msg_removemenufail", 'c', '4', arg2);
        } else if (ReActions.getActivators().containsActivator(arg1)) {
            Logic act = ReActions.getActivators().getActivator(arg1).getLogic();
            if (NumberUtils.isNumber(arg3.toString(), Is.POSITIVE_NATURAL)) {
                int num = Integer.parseInt(arg3.toString());
                if (arg2.equalsIgnoreCase("f") || arg2.equalsIgnoreCase("flag")) {
                    if (act.removeFlag(num - 1))
                        Msg.printMSG(sender, "msg_flagremoved", act.getName(), num);
                    else Msg.printMSG(sender, "msg_failedtoremoveflag", act.getName(), num);
                } else if (arg2.equalsIgnoreCase("a") || arg2.equalsIgnoreCase("action")) {
                    if (act.removeAction(num - 1))
                        Msg.printMSG(sender, "msg_actionremoved", act.getName(), num);
                    else Msg.printMSG(sender, "msg_failedtoremoveaction", act.getName(), num);
                } else if (arg2.equalsIgnoreCase("r") || arg2.equalsIgnoreCase("reaction")) {
                    if (act.removeReaction(num - 1))
                        Msg.printMSG(sender, "msg_reactionremoved", act.getName(), num);
                    else Msg.printMSG(sender, "msg_failedtoremovereaction", act.getName(), num);
                } else return false;
                ReActions.getActivators().saveGroup(act.getGroup());
            } else Msg.printMSG(sender, "msg_wrongnumber", arg3.toString());
        }
        return true;
    }

    private boolean removeVariable(CommandSender sender, String param) {
        Player p = (sender instanceof Player) ? (Player) sender : null;
        Parameters params = Parameters.fromString(param);
        String player = params.getString("player", "");
        if (player.equalsIgnoreCase("%player%") && p != null) player = p.getName();
        String id = params.getString("id", "");
        if (id.isEmpty()) {
            return Msg.MSG_VARNEEDID.print(sender);
        }
        if (ReActions.getVariables().removeVariable(player, id)) {
            return Msg.MSG_VARREMOVED.print(sender, id);
        }
        return Msg.MSG_VARREMOVEFAIL.print(sender);
    }

}
