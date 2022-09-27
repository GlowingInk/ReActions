package me.fromgate.reactions.commands;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.commands.custom.FakeCommander;
import me.fromgate.reactions.holders.LocationHolder;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.menu.InventoryMenu;
import me.fromgate.reactions.time.Delayer;
import me.fromgate.reactions.time.TimersManager;
import me.fromgate.reactions.util.math.NumberUtils;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;


@CmdDefine(command = "reactions", description = Msg.CMD_LIST, permission = "reactions.config",
        subCommands = {"list"}, allowConsole = true, shortDescription = "&3/react list [loc|group|type] [page]")
public class CmdList extends Cmd {


    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        int lpp = (player == null) ? 1000 : 15;
        int page = 1;
        String arg1 = args.length >= 2 ? args[1].toLowerCase(Locale.ENGLISH) : "";
        String arg2 = args.length >= 3 ? args[2] : "";
        String arg3 = args.length >= 4 ? args[3] : "";
        if (NumberUtils.isNonzeroInteger(arg1)) printAct(sender, 1, lpp);
        else {
            String mask = "";
            if (NumberUtils.isNonzeroInteger(arg2)) {
                page = Integer.parseInt(arg2);
                mask = arg3;
            } else if (NumberUtils.isNonzeroInteger(arg3)) {
                page = Integer.parseInt(arg3);
                mask = arg2;
            }

            switch (arg1) {
                case "type" -> printActType(sender, mask, page, lpp);
                case "group" -> printActGroup(sender, mask, page, lpp);
                case "timer", "timers" -> TimersManager.listTimers(sender, page);
                case "delay", "delays" -> Delayer.printDelayList(sender, page, lpp);
                case "loc", "location" -> LocationHolder.printLocList(sender, page, lpp);
                case "var", "variables", "variable" -> ReActions.getVariables().printList(sender, page, mask);
                case "menu", "menus" -> InventoryMenu.printMenuList(sender, page, mask);
                case "cmd", "commands" -> FakeCommander.list().forEach(sender::sendMessage);
                default -> printAct(sender, page, lpp);
            }
        }
        return true;
    }

    private void printAct(CommandSender sender, int page, int lpp) {
        Collection<Activator> activators = ReActions.getActivators().search().all();
        List<String> ag = new ArrayList<>(activators.size());
        activators.forEach(a -> ag.add(a.getLogic().getName()));
        Msg.printPage(sender, ag, Msg.MSG_ACTLIST, page, lpp, true);
        Msg.MSG_LISTCOUNT.print(sender, ag.size(), LocationHolder.sizeTpLoc());
    }

    private void printActGroup(CommandSender sender, String group, int page, int lpp) {
        Collection<Activator> activators = ReActions.getActivators().search().byGroup(group);
        List<String> ag = new ArrayList<>(activators.size());
        activators.forEach(a -> ag.add(a.getLogic().getName()));
        Msg.MSG_ACTLISTGRP.print(sender, group, '6', '6');
        Msg.printPage(sender, ag, null, page, lpp, true);
    }

    private void printActType(CommandSender sender, String type, int page, int lpp) {
        Collection<Activator> activators = ReActions.getActivators().search().byType(type);
        List<String> ag = new ArrayList<>(activators.size());
        activators.forEach(a -> ag.add(a.getLogic().getName()));
        Msg.MSG_ACTLISTTYPE.print(sender, type, '6', '6');
        Msg.printPage(sender, ag, null, page, lpp, true);
    }


}
