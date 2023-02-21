package fun.reactions.commands;

import fun.reactions.holders.LocationHolder;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.message.Msg;
import org.bukkit.entity.Player;

@CmdDefine(command = "reactions", description = Msg.CMD_SELECT, permission = "reactions.select",
        subCommands = {"select|sel"}, shortDescription = "&3/react select")
public class CmdSelect extends Cmd {
    @Override
    public boolean execute(Player player, String[] args) {
        LocationHolder.hold(player, null);
        Msg.CMD_SELECTED.print(player, LocationUtils.locationToStringFormatted(LocationHolder.getHeld(player)));
        return true;
    }
}
