package me.fromgate.reactions.commands;

import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.commands.custom.FakeCommander;
import me.fromgate.reactions.externals.worldguard.RaWorldGuard;
import me.fromgate.reactions.holders.LocationHolder;
import me.fromgate.reactions.menu.InventoryMenu;
import me.fromgate.reactions.time.LazyDelayManager;
import me.fromgate.reactions.time.timers.TimersManager;
import me.fromgate.reactions.util.message.Msg;
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
                FakeCommander.updateCommands();
            }
            if (check.contains("d"))
                LazyDelayManager.load();
            if (check.contains("v")) {
                if (!Cfg.playerSelfVarFile) ReActions.getVariables().load();
                else ReActions.getVariables().loadVars();
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
            LazyDelayManager.load();
            if (!Cfg.playerSelfVarFile) ReActions.getVariables().load();
            else ReActions.getVariables().loadVars();
            TimersManager.init();
            InventoryMenu.load();
            FakeCommander.updateCommands();
        }
        Msg.MSG_CMDRELOAD.print(sender, ReActions.getActivators().search().all().size(), LocationHolder.sizeTpLoc());
        return true;
    }

}
