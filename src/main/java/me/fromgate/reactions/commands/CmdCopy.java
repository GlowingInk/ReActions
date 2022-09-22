package me.fromgate.reactions.commands;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.ActivatorsManager;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;

@CmdDefine(command = "reactions", description = Msg.CMD_COPY, permission = "reactions.config",
        subCommands = {"copy"}, allowConsole = true, shortDescription = "&3/react copy [f|a|r] <source> <destination>")
public class CmdCopy extends Cmd {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 3 && args.length != 4) return false;
        String id1 = args.length == 4 ? args[2] : args[1];
        String id2 = args.length == 4 ? args[3] : args[2];
        String copyMode = args.length == 3 ? "all" : args[1];
        ActivatorsManager activators = ReActions.getActivators();
        Activator actFrom = activators.getActivator(id1);
        Activator actTo = activators.getActivator(id2);
        boolean fail = actFrom == null || actTo == null;
        switch (copyMode.toLowerCase(Locale.ENGLISH)) {
            case "f": case "flag": case "flags":
                if (fail) {
                    Msg.printMSG(sender, "msg_copyflagsfailed", 'c', '4', id1, id2);
                } else {
                    copy(actFrom.getLogic().getFlags(), actTo.getLogic().getFlags());
                    Msg.printMSG(sender, "msg_copyflags", id1, id2);
                }
                break;

            case "a": case "action": case "actions":
                if (fail) {
                    Msg.printMSG(sender, "msg_copyactionsfailed", 'c', '4', id1, id2);
                } else {
                    copy(actFrom.getLogic().getActions(), actTo.getLogic().getActions());
                    Msg.printMSG(sender, "msg_copyactions", id1, id2);
                }
                break;

            case "r": case "reaction": case "reactions":
                if (fail) {
                    Msg.printMSG(sender, "msg_copyreactionsfailed", 'c', '4', id1, id2);
                } else {
                    copy(actFrom.getLogic().getReactions(), actTo.getLogic().getReactions());
                    Msg.printMSG(sender, "msg_copyreactions", id1, id2);
                }
                break;

            default:
                if (fail) {
                    Msg.printMSG(sender, "msg_copyallfailed", 'c', '4', id1, id2);
                } else {
                    copy(actFrom.getLogic().getFlags(), actTo.getLogic().getFlags());
                    copy(actFrom.getLogic().getActions(), actTo.getLogic().getActions());
                    copy(actFrom.getLogic().getReactions(), actTo.getLogic().getReactions());
                    Msg.printMSG(sender, "msg_copyall", id1, id2);
                }
                break;
        }
        if (!fail) activators.saveGroup(actTo.getLogic().getGroup());
        return true;
    }

    private static <T> void copy(List<T> from, List<T> to) {
        to.clear();
        to.addAll(from);
    }
}
