package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.activators.ActivatorsManager;
import fun.reactions.util.message.Msg;
import org.bukkit.command.CommandSender;

@CmdDefine(command = "reactions", description = Msg.CMD_GROUP, permission = "reactions.config",
        subCommands = {"group"}, allowConsole = true, shortDescription = "&3/react group <activator> <groupname>")
public class CmdGroup extends Cmd {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String id = args.length > 1 ? args[1] : "";
        if (id.isEmpty()) return false;
        String group = args.length > 2 ? args[2] : "activators";
        ActivatorsManager activators = ReActions.getActivators();
        Activator activator = activators.getActivator(id);
        if (activator != null && activators.moveActivator(activator, group)) {
            Msg.printMSG(sender, "msg_groupset", id, group);
        } else {
            Msg.printMSG(sender, "msg_groupsetfailed", id, group);
        }
        return true;
    }
}
