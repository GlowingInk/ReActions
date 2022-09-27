package me.fromgate.reactions.commands;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.util.message.BukkitMessenger;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: Refactor a whole commands system
public class Commander implements CommandExecutor {
    private static final Set<Cmd> commands = new HashSet<>();
    private static JavaPlugin plugin;
    private static Commander commander;

    public static void init(JavaPlugin plg) {
        plugin = plg;
        commander = new Commander();

        addNewCommand(new CmdHelp());
        addNewCommand(new CmdRun());
        addNewCommand(new CmdAdd());
        addNewCommand(new CmdCreate());
        addNewCommand(new CmdSet());
        addNewCommand(new CmdCopy());
        addNewCommand(new CmdList());
        addNewCommand(new CmdInfo());
        addNewCommand(new CmdGroup());
        addNewCommand(new CmdRemove());
        addNewCommand(new CmdClear());
        addNewCommand(new CmdSelect());
        addNewCommand(new CmdDebug());
        addNewCommand(new CmdCheck());
        addNewCommand(new CmdReload());
        addNewCommand(new CmdExec());
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    private static boolean addNewCommand(Cmd cmd) {
        if (cmd.getCommand() == null) return false;
        if (cmd.getCommand().isEmpty()) return false;
        plugin.getCommand(cmd.getCommand()).setExecutor(commander);
        commands.add(cmd);
        return true;
    }

    public static void printHelp(CommandSender sender, int page) {
        List<String> helpList = new ArrayList<>();
        for (Cmd cmd : commands) {
            helpList.add(cmd.getFullDescription());
        }
        Msg.printMessage(sender, "&6&lReActions v" + ReActions.getPlugin().getDescription().getVersion() + " &r&6| " + Msg.HLP_HELP.getText("NO_COLOR"));
        BukkitMessenger.printPage(sender, helpList, null, page);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cmdLabel, String[] args) {
        for (Cmd cmd : commands) {
            if (!cmd.getCommand().equalsIgnoreCase(command.getLabel())) continue;
            if (cmd.executeCommand(sender, args)) return true;
        }
        return false;
    }


}
