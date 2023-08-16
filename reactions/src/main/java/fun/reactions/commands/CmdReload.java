package fun.reactions.commands;

import fun.reactions.Cfg;
import fun.reactions.ReActions;
import fun.reactions.holders.LocationHolder;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.module.worldguard.external.RaWorldGuard;
import fun.reactions.time.CooldownManager;
import fun.reactions.time.timers.TimersManager;
import fun.reactions.util.message.Msg;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Locale;

@CmdDefine(command = "reactions", description = Msg.CMD_RELOAD, permission = "reactions.config",
        subCommands = {"reload"}, allowConsole = true, shortDescription = "&3/react reload [galcdvtm] [groupId]")
public class CmdReload extends Cmd {

    @Override
    public boolean execute(CommandSender sender, String[] params) {
        if (params.length >= 2) {
            String check = params[1].toLowerCase(Locale.ROOT);
            if (check.contains("g") && params.length > 2) {
                ReActions.getActivators().loadGroup(params[2].replaceAll("[/\\\\]", File.separator), true);
                RaWorldGuard.updateRegionCache();
            } else if (check.contains("a")) {
                ReActions.getActivators().clearActivators();
                ReActions.getActivators().loadGroup("", false);
                RaWorldGuard.updateRegionCache();
            }
            if (check.contains("l"))
                LocationHolder.loadLocs();
            if (check.contains("c")) {
                ReActions.getPlugin().reloadConfig();
                Cfg.load(ReActions.getPlugin().getConfig());
                ReActions.getCommands().reload();
            }
            if (check.contains("d"))
                CooldownManager.load();
            if (check.contains("v")) {
                if (!Cfg.playerSelfVarFile) ReActions.getPersistentVariables().load();
                else ReActions.getPersistentVariables().loadVars();
            }
            if (check.contains("t"))
                TimersManager.init();
            if (check.contains("m"))
                InventoryMenu.load();

        } else {
            ReActions.getActivators().clearActivators();
            ReActions.getActivators().loadGroup("", false);
            RaWorldGuard.updateRegionCache();
            LocationHolder.loadLocs();
            ReActions.getPlugin().reloadConfig();
            Cfg.load(ReActions.getPlugin().getConfig());
            CooldownManager.load();
            if (!Cfg.playerSelfVarFile) ReActions.getPersistentVariables().load();
            else ReActions.getPersistentVariables().loadVars();
            TimersManager.init();
            InventoryMenu.load();
            ReActions.getCommands().reload();
        }
        Msg.MSG_CMDRELOAD.print(sender, ReActions.getActivators().search().all().size(), LocationHolder.sizeTpLoc());
        return true;
    }

}
