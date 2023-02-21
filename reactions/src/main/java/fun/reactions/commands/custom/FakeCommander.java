package fun.reactions.commands.custom;

import fun.reactions.logic.environment.Variables;
import fun.reactions.module.basics.ContextManager;
import fun.reactions.module.basics.context.CommandContext;
import fun.reactions.util.FileUtils;
import fun.reactions.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

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
    private static final Map<String, UserCommand> commands = new HashMap<>();
    private static File file;

    private FakeCommander() {}

    public static void init(Plugin plugin) {
        file = new File(plugin.getDataFolder(), "commands.yml");
        if (!file.exists()) plugin.saveResource("commands.yml", false);
        updateCommands();
    }

    public static void updateCommands() {
        YamlConfiguration cfg = new YamlConfiguration();
        if (!FileUtils.loadCfg(cfg, file, "Failed to load commands")) return;
        commands.clear();
        for (String cmdKey : cfg.getKeys(false)) {
            ConfigurationSection cmdSection = cfg.getConfigurationSection(cmdKey);
            if (cmdSection == null) continue;
            String command = cmdSection.getString("command");
            // TODO: Error message
            if (command == null) continue;
            String prefix = cmdSection.getString("prefix");
            List<String> aliases = cmdSection.getStringList("alias");
            boolean toBukkit = cmdSection.getBoolean("register", true);
            // TODO: Error message
            register(command, prefix, aliases, new UserCommand(cmdSection, toBukkit), toBukkit);
        }
    }

    public static boolean triggerRaCommand(CommandContext storage, boolean activated) {
        UserCommand raCmd = commands.get(storage.getLabel().toLowerCase(Locale.ROOT));
        if (raCmd == null) return false;
        String exec = raCmd.executeCommand(storage.getSender(), storage.getArgs());
        if (exec != null) {
            if (!activated) storage.initialize();
            ContextManager.triggerExec(storage.getSender(), exec, storage.getVariables().orElse(new Variables()));
        }
        // It's not activator - context will not be generated
        return raCmd.isOverride();
    }

    private static boolean register(String command, String prefix, List<String> aliases, UserCommand userCommand, boolean toBukkit) {
        if (Utils.isStringEmpty(command)) return false;
        command = command.toLowerCase(Locale.ROOT);
        prefix = Utils.isStringEmpty(prefix) ? command : prefix.toLowerCase(Locale.ROOT);
        // Registering main command
        if (toBukkit) Bukkit.getCommandMap().register(prefix, userCommand);
        commands.put(command, userCommand);
        commands.put(prefix + ":" + command, userCommand);
        // Registering aliases
        for (String alias : aliases) {
            if (toBukkit) Bukkit.getCommandMap().register(alias, prefix, userCommand);
            commands.put(alias, userCommand);
            commands.put(prefix + ":" + alias, userCommand);
        }
        return true;
    }

    private static Set<UserCommand> getCommandsSet() {
        return new HashSet<>(commands.values());
    }

    public static List<String> list() {
        List<String> list = new ArrayList<>();
        for (UserCommand cmd : getCommandsSet()) {
            List<String> sublist = cmd.list();
            sublist.forEach(s -> list.add(ChatColor.UNDERLINE + "/" + cmd.getName() + ChatColor.RESET + " " + s));
        }
        return list;
    }
}
