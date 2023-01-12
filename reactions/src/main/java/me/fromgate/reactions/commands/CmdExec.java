package me.fromgate.reactions.commands;

import me.fromgate.reactions.module.basics.DetailsManager;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
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
        if (DetailsManager.triggerExec(sender, param)) {
            Msg.printMSG(sender, "cmd_runplayer", commandLine);
        } else Msg.printMSG(sender, "cmd_runplayerfail", 'c', '6', commandLine);
        return true;
    }

}
