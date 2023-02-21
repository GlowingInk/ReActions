package fun.reactions.commands;

import fun.reactions.ReActions;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activity.flags.StoredFlag;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.message.Msg;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

@CmdDefine(command = "reactions", description = Msg.CMD_INFO, permission = "reactions.config",
        subCommands = {"info"}, allowConsole = true, shortDescription = "&3/react info <activator> [f|a|r]")
public class CmdInfo extends Cmd {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String id = args.length > 1 ? args[1] : "";
        if (id.isEmpty()) return false;
        String far = args.length > 2 ? args[2] : "";
        if (ReActions.getActivators().containsActivator(id)) {
            printActInfo(sender, id, far);
        } else if (id.equalsIgnoreCase("menu")) {
            InventoryMenu.printMenu(sender, far);
        } else Msg.printMSG(sender, "cmd_unknownbutton", id);
        return true;
    }


    private void printActInfo(CommandSender sender, String activatorName, String far) {
        Activator act = ReActions.getActivators().getActivator(activatorName);
        Logic base = act.getLogic();
        boolean f;
        boolean a;
        boolean r;
        if (far.isEmpty()) {
            f = true;
            a = true;
            r = true;
        } else {
            f = far.contains("f") || far.equalsIgnoreCase("flag") || far.equalsIgnoreCase("flags");
            a = far.contains("a") || far.equalsIgnoreCase("action") || far.equalsIgnoreCase("actions");
            r = far.contains("r") || far.equalsIgnoreCase("reaction") || far.equalsIgnoreCase("reactions");
        }

        Msg.printMSG(sender, "&5☆ &d&l" + Msg.MSG_ACTINFOTITLE.getText("NOCOLOR") + " &r&5☆");
        Msg.printMSG(sender, "msg_actinfo", base.getName(), base.getType(), base.getGroup());
        Msg.printMSG(sender, "msg_actinfo2", base.getFlags().size(), base.getActions().size(), base.getReactions().size());
        if (f && (!base.getFlags().isEmpty())) {
            List<String> flg = new ArrayList<>();
            for (int i = 0; i < base.getFlags().size(); i++) {
                StoredFlag flag = base.getFlags().get(i);
                flg.add((flag.isInverted() ? "&4! &e" : "  &e") + flag.getActivity().getName() + " &3= &a" + flag.getParameters());
            }
            Msg.printPage(sender, flg, Msg.LST_FLAGS, 1, 100, true);
        }
        if (a && (!base.getActions().isEmpty())) {
            List<String> flg = new ArrayList<>();
            for (int i = 0; i < base.getActions().size(); i++) {
                String action = base.getActions().get(i).getActivity().getName();
                String param = base.getActions().get(i).getParameters();
                if (action.equalsIgnoreCase("tp")) {
                    Location loc = LocationUtils.parseCoordinates(param);//Util.parseLocation(param);
                    if (loc != null) param = LocationUtils.locationToStringFormatted(loc);
                }
                flg.add("  &e" + action + " &3= &a" + param);
            }
            Msg.printPage(sender, flg, Msg.LST_ACTIONS, 1, 100, true);
        }
        if (r && (!base.getReactions().isEmpty())) {
            List<String> flg = new ArrayList<>();
            for (int i = 0; i < base.getReactions().size(); i++) {
                String action = base.getReactions().get(i).getActivity().getName();
                String param = base.getReactions().get(i).getParameters();
                if (action.equalsIgnoreCase("tp")) {
                    Location loc = LocationUtils.parseCoordinates(param);
                    if (loc != null) param = LocationUtils.locationToStringFormatted(loc);
                }
                flg.add("  &e" + action + " &3= &a" + param);
            }
            Msg.printPage(sender, flg, Msg.LST_REACTIONS, 1, 100, true);
        }
    }
}
