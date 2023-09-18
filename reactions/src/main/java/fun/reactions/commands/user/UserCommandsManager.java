package fun.reactions.commands.user;

import fun.reactions.ReActions;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.module.basic.activators.CommandActivator;
import fun.reactions.util.FileUtils;
import fun.reactions.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// TODO Use RaCommand
public final class UserCommandsManager implements Listener {
    private final ReActions.Platform platform;
    private final Map<String, UserCommand> commands;
    private final File file;

    public UserCommandsManager(@NotNull ReActions.Platform platform) {
        this.platform = platform;
        this.commands = new HashMap<>();
        this.file = new File(platform.getDataFolder(), "commands.yml");
        if (!file.exists()) platform.getPlugin().saveResource("commands.yml", false);
    }

    public void reload() {
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

    @EventHandler(ignoreCancelled = true)
    public void onCommand(@NotNull PlayerCommandPreprocessEvent event) {
        if (triggerRaCommand(new CommandActivator.Context(
                event.getPlayer(), event.getPlayer(), event.getMessage().substring(1))
        )) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        if (triggerRaCommand(new CommandActivator.Context(
                null, event.getSender(), event.getCommand()
        ))) event.setCancelled(true);
    }

    private boolean triggerRaCommand(@NotNull CommandActivator.Context context) {
        UserCommand raCmd = commands.get(context.getLabel().toLowerCase(Locale.ROOT));
        if (raCmd == null) return false;
        String exec = raCmd.executeCommand(context.getSender(), context.getArgs());
        if (exec != null) {
            context.initialize();
            Activator activator = platform.getActivators().getActivator(exec);
            if (activator == null) {
                platform.logger().warn("There's no activators named " + exec);
                return false;
            }
            Logic logic = activator.getLogic();
            logic.execute(context.createEnvironment(
                    platform,
                    logic.getName()
            ));
        }
        return raCmd.isOverride();
    }

    private boolean register(String command, String prefix, List<String> aliases, UserCommand userCommand, boolean toBukkit) {
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

    private @NotNull Set<UserCommand> getCommandsSet() {
        return new HashSet<>(commands.values());
    }

    public @NotNull List<String> list() {
        List<String> list = new ArrayList<>();
        for (UserCommand cmd : getCommandsSet()) {
            List<String> sublist = cmd.list();
            sublist.forEach(s -> list.add(ChatColor.UNDERLINE + "/" + cmd.getName() + ChatColor.RESET + " " + s));
        }
        return list;
    }
}
