package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.time.CooldownManager;
import fun.reactions.util.message.Msg;
import fun.reactions.util.parameter.Parameters;
import fun.reactions.util.time.TimeUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;


@CmdDefine(command = "reactions", description = Msg.CMD_SET, permission = "reactions.config", subCommands = {"set"}, allowConsole = true, shortDescription = "&3/react set delay|var id:<id> player:<player> delay:<time>")
public class CmdSet extends Cmd {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 1) return false;
        String arg1 = args[1];
        String arg2 = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
        return this.setVariable(sender, arg1, arg2);
    }

    private boolean setVariable(CommandSender sender, String var, String param) {
        Player p = (sender instanceof Player) ? (Player) sender : null;
        Parameters params = Parameters.fromString(param, "id");
        String id = params.getString("id", "");
        if (id.isEmpty()) {
            Msg.MSG_NEEDVDMID.print(sender, 'c');
            return true;
        }
        boolean msg = !params.getBoolean("silent", false);
        if (var.equalsIgnoreCase("delay") || var.equalsIgnoreCase("d")) {
            boolean add = params.getBoolean("add", false);
            String player = params.getString("player", "");
            if (player.equalsIgnoreCase("%player%") && (p != null)) player = p.getName();
            long time = params.getTime("delay");
            if (player.isEmpty()) CooldownManager.setCooldown(id, time, add);
            else CooldownManager.setPersonalCooldown(player, id, time, add);
            if (msg) Msg.printMSG(sender, "cmd_delayset", player.isEmpty() ? id : player + "." + id, TimeUtils.formatTime(TimeUtils.addOffset(time)));
            return true;
        } else if (var.equalsIgnoreCase("var") || var.equalsIgnoreCase("variable") || var.equalsIgnoreCase("v")) {
            String value = params.getString("value", "");
            String player = params.getString("player", "");
            ReActions.getPersistentVariables().setVariable(player, id, value);
            if (msg) Msg.CMD_VARSET.print(sender, player.isEmpty() ? id : player + "." + id, ReActions.getPersistentVariables().getVariable(player, id));
            return true;
        } else if (var.equalsIgnoreCase("menu") || var.equalsIgnoreCase("m")) {
            if (InventoryMenu.set(id, params)) {
                if (msg) Msg.MSG_MENUPARAMSET.print(sender, id);
            } else {
                if (msg) Msg.MSG_MENUSETFAIL.print(sender, 'c', '4', id);
            }
            return true;
        } else {
            return false;
        }
    }

}
