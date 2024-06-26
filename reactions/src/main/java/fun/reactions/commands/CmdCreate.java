package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.holders.LocationHolder;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.time.timers.TimersManager;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.message.Msg;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.BlockParameters;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

@CmdDefine(command = "reactions", description = Msg.CMD_CREATE, permission = "reactions.config",
        subCommands = {"create"}, allowConsole = true,
        shortDescription = "&3/react create <loc|timer|menu|activatorType> <id> [param]")
public class CmdCreate extends Cmd {
    // TODO: Commands creation
    // TODO: Cuboids creation
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) return false;
        Player player = (sender instanceof Player) ? (Player) sender : null;
        String type = args[1].toLowerCase(Locale.ROOT);
        String id = args[2];
        StringBuilder param = new StringBuilder(args.length >= 4 ? args[3] : "");
        if (args.length > 4) {
            for (int i = 4; i < args.length; i++)
                param.append(" ").append(args[i]);
            param = new StringBuilder(param.toString().trim());
        }
        switch (type) {
            case "loc":
                if (player == null) return false;
                if (!LocationHolder.addTpLoc(id, player.getLocation())) return false;
                LocationHolder.saveLocs();
                Msg.CMD_ADDTPADDED.print(sender, id);
                return true;
            case "timer":
                if (param.isEmpty()) return false;
                return TimersManager.addTimer(sender, id, Parameters.fromString(param.toString()), true);
            case "menu":
                // TODO: Create menu from chest
                if (param.isEmpty()) return false;
                String arg3 = args[3];
                if (InventoryMenu.add(id,
                        NumberUtils.parseInteger(arg3, Is.NON_NEGATIVE).orElse(9),
                        (param.length() == 1) ? "" : param.substring(arg3.length()))) {
                    Msg.CMD_ADDMENUADDED.print(sender, id);
                    return true;
                }
                Msg.CMD_ADDMENUADDFAIL.print(sender, id);
                return false;
            default:
                return addActivator(sender, type, id, param.toString());
        }
    }

    private boolean addActivator(CommandSender sender, String typeStr, String name, String param) {
        ActivatorsManager activators = ReActions.getActivators();
        ActivatorType type = ReActions.getActivatorTypes().get(typeStr);
        if (type == null) return false;
        Parameters params;
        if (sender instanceof Player player) {
            param = LocationUtils.parsePlaceholders(player, param);
            if (type.isNeedBlock())
                params = new BlockParameters(param, player.getTargetBlock(null, 100));
            else
                params = Parameters.fromString(param);
        } else {
            if (type.isNeedBlock()) return false;
            params = Parameters.fromString(param);
        }
        Logic logic = new Logic(ReActions.getPlatform(), type.getName().toUpperCase(Locale.ROOT), name);
        Activator activator = type.createActivator(logic, params);
        if (activator == null || !activator.isValid()) {
            Msg.CMD_NOTADDBADDEDSYNTAX.print(sender, name, type);
            return true;
        }
        if (activators.addActivator(activator, true)) {
            activators.saveGroup(activator.getLogic().getGroup());
            Msg.CMD_ADDBADDED.print(sender, activator.toString());
        } else {
            Msg.CMD_NOTADDBADDED.print(sender, activator.toString());
        }
        return true;
    }
}
