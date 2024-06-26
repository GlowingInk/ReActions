package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.util.message.Msg;
import org.bukkit.command.CommandSender;

@CmdDefine(command = "reactions", description = Msg.CMD_CLEAR, permission = "reactions.config",
        subCommands = {"clear"}, allowConsole = true, shortDescription = "&3/react clear <id> [f|a|r]")
public class CmdClear extends Cmd {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String activatorId = args.length >= 2 ? args[1] : "";
        if (activatorId.isEmpty()) return false;
        String arg2 = args.length >= 3 ? args[2] : "";
        Activator activator = ReActions.getActivators().getActivator(activatorId);
        if (activator != null) {
            Logic logic = activator.getLogic();
            if (arg2.equalsIgnoreCase("f") || arg2.equalsIgnoreCase("flag")) {
                logic.clearFlags();
                Msg.MSG_CLEARFLAG.print(sender, activatorId);
            } else if (arg2.equalsIgnoreCase("a") || arg2.equalsIgnoreCase("action")) {
                logic.clearActions();
                Msg.MSG_CLEARACT.print(sender, activatorId);
            } else if (arg2.equalsIgnoreCase("r") || arg2.equalsIgnoreCase("reaction")) {
                logic.clearReactions();
                Msg.MSG_CLEARREACT.print(sender, activatorId);
            }
            ReActions.getActivators().saveGroup(logic.getGroup());
        } else {
            Msg.CMD_UNKNOWNBUTTON.print(sender, activatorId);
        }
        return true;
    }
}
