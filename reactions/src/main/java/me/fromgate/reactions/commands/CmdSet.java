package me.fromgate.reactions.commands;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.menu.InventoryMenu;
import me.fromgate.reactions.time.CooldownManager;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
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
        if (id.isEmpty()) return Msg.MSG_NEEDVDMID.print(sender, 'c');
        if (var.equalsIgnoreCase("delay") || var.equalsIgnoreCase("d")) {
            boolean add = params.getBoolean("add", false);
            String player = params.getString("player", "");
            if (player.equalsIgnoreCase("%player%") && (p != null)) player = p.getName();
            long time = /*System.currentTimeMillis()+*/TimeUtils.parseTime(params.getString("delay", "3s")); //дефолтная задержка три секунды
            if (player.isEmpty()) CooldownManager.setCooldown(id, time, add);
            else CooldownManager.setPersonalCooldown(player, id, time, add);
            Msg.printMSG(sender, "cmd_delayset", player.isEmpty() ? id : player + "." + id, TimeUtils.formatTime(System.currentTimeMillis() + time));
        } else if (var.equalsIgnoreCase("var") || var.equalsIgnoreCase("variable") || var.equalsIgnoreCase("v")) {
            String value = params.getString("value", "");
            String player = params.getString("player", "");
            ReActions.getVariables().setVariable(player, id, value);
            return Msg.CMD_VARSET.print(sender, player.isEmpty() ? id : player + "." + id, ReActions.getVariables().getVariable(player, id));
        } else if (var.equalsIgnoreCase("menu") || var.equalsIgnoreCase("m")) {
            if (InventoryMenu.set(id, params))
                return Msg.MSG_MENUPARAMSET.print(sender, id);
            else return Msg.MSG_MENUSETFAIL.print(sender, 'c', '4', id);
        } else return false;
        return true;
    }

}
