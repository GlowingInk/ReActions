package me.fromgate.reactions.commands.custom;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.module.basics.StoragesManager;
import me.fromgate.reactions.module.basics.storages.*;
import me.fromgate.reactions.util.FileUtils;
import me.fromgate.reactions.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// TODO: Remove statics
public final class FakeCommander {
    // TODO: Use Paper's async tab completer
    private static final Map<String, RaCommand> commands = new HashMap<>();

    private FakeCommander() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static void init() {
        ReActions.getPlugin().saveResource("commands.yml", false);
        updateCommands();
    }

    public static void updateCommands() {
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "commands.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        if (!FileUtils.loadCfg(cfg, f, "Failed to load commands")) return;
        CommandMap commandMap = Bukkit.getCommandMap();
        commands.clear();
        for (String cmdKey : cfg.getKeys(false)) {
            ConfigurationSection cmdSection = cfg.getConfigurationSection(cmdKey);
            String command = cmdSection.getString("command");
            // TODO: Error message
            if (command == null) continue;
            String prefix = cmdSection.getString("prefix");
            List<String> aliases = cmdSection.getStringList("alias");
            boolean toBukkit = cmdSection.getBoolean("register", true);
            // TODO: Error message
            register(command, prefix, aliases, commandMap, new RaCommand(cmdSection, toBukkit), toBukkit);
        }
    }

    public static boolean raiseRaCommand(CommandStorage storage, boolean activated) {
        RaCommand raCmd = commands.get(storage.getLabel().toLowerCase(Locale.ENGLISH));
        if (raCmd == null) return false;
        String exec = raCmd.executeCommand(storage.getSender(), storage.getArgs());
        if (exec != null) {
            if (!activated) storage.init();
            StoragesManager.triggerExec(storage.getSender(), exec, storage.getVariables());
        }
        // It's not activator - context will not be generated
        return raCmd.isOverride();
    }

    private static boolean register(String command, String prefix, List<String> aliases, CommandMap commandMap, RaCommand raCommand, boolean toBukkit) {
        if (Utils.isStringEmpty(command)) return false;
        command = command.toLowerCase(Locale.ENGLISH);
        prefix = Utils.isStringEmpty(prefix) ? command : prefix.toLowerCase(Locale.ENGLISH);
        if (aliases == null)
            aliases = new ArrayList<>();
        // Registering main command
        if (toBukkit) commandMap.register(prefix, raCommand);
        commands.put(command, raCommand);
        commands.put(prefix + ":" + command, raCommand);
        // Registering aliases
        for (String alias : aliases) {
            if (toBukkit) commandMap.register(alias, prefix, raCommand);
            commands.put(alias, raCommand);
            commands.put(prefix + ":" + alias, raCommand);
        }
        return true;
    }

    private static Set<RaCommand> getCommandsSet() {
        return new HashSet<>(commands.values());
    }

    public static List<String> list() {
        List<String> list = new ArrayList<>();
        for (RaCommand cmd : getCommandsSet()) {
            List<String> sublist = cmd.list();
            sublist.forEach(s -> list.add(ChatColor.UNDERLINE + "/" + cmd.getName() + ChatColor.RESET + " " + s));
        }
        return list;
    }
}
