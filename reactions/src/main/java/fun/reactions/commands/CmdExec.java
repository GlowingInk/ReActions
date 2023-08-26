package fun.reactions.commands;

import fun.reactions.module.basic.ContextManager;
import fun.reactions.util.message.Msg;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.command.CommandSender;

@CmdDefine(command = "exec", description = Msg.CMD_EXEC, permission = "reactions.run",
        subCommands = {}, allowConsole = true,
        shortDescription = "&3/exec <activator> [player:<PlayerSelector>] [delay:<Time>]")
public class CmdExec extends Cmd {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) return false;
        String commandLine = String.join(" ", args);
        Parameters param = Parameters.fromString(commandLine, "activator");
        if (ContextManager.triggerFunction(sender, param)) {
            Msg.printMSG(sender, "cmd_runplayer", commandLine);
        } else Msg.printMSG(sender, "cmd_runplayerfail", 'c', '6', commandLine);
        return true;
    }
}
