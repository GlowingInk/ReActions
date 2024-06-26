package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.holders.LocationHolder;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.activators.Activator;
import fun.reactions.time.CooldownManager;
import fun.reactions.time.timers.TimersManager;
import fun.reactions.util.message.Msg;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;


@CmdDefine(command = "reactions", description = Msg.CMD_LIST, permission = "reactions.config",
        subCommands = {"list"}, allowConsole = true, shortDescription = "&3/react list [loc|group|type] [page]")
public class CmdList extends Cmd { // I don't know what's going on here...
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        int lpp = (player == null) ? 1000 : 15;
        int page = 1;
        String arg1 = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "";
        String arg2 = args.length >= 3 ? args[2] : "";
        String arg3 = args.length >= 4 ? args[3] : "";

        OptionalInt opt = NumberUtils.parseInteger(arg1, Is.POSITIVE);
        if (opt.isPresent()) {
            printAct(sender, opt.getAsInt(), lpp);
        } else {
            String mask = "";
            opt = NumberUtils.parseInteger(arg2, Is.POSITIVE);
            if (opt.isPresent()) {
                page = opt.getAsInt();
                mask = arg3;
            } else {
                opt = NumberUtils.parseInteger(arg3, Is.POSITIVE);
                if (opt.isPresent()) {
                    page = opt.getAsInt();
                    mask = arg2;
                }
            }

            switch (arg1) {
                case "type" -> printActType(sender, mask, page, lpp);
                case "group" -> printActGroup(sender, mask, page, lpp);
                case "timer", "timers" -> TimersManager.listTimers(sender, page);
                case "delay", "delays" -> CooldownManager.printCooldownList(sender, page, lpp);
                case "loc", "location" -> LocationHolder.printLocList(sender, page, lpp);
                case "var", "variables", "variable" -> ReActions.getPersistentVariables().printList(sender, page, mask);
                case "menu", "menus" -> InventoryMenu.printMenuList(sender, page, mask);
                case "cmd", "commands" -> ReActions.getCommands().list().forEach(sender::sendMessage);
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
