package me.fromgate.reactions.commands.custom;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.util.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Custom implementation of Bukkit Command
 */
@ApiStatus.Internal
public final class UserCommand extends Command implements PluginIdentifiableCommand {
    private final String permission;
    private final boolean consoleAllowed;
    private final boolean override;
    private final boolean tab;
    // EXEC activators
    private final Map<ExecType, String> execs;
    // Sorted by priority
    private final SortedSet<ArgumentsChain> chains;

    public UserCommand(ConfigurationSection cmdSection, boolean register) {
        super(Objects.requireNonNull(cmdSection.getString("command")));
        this.permission = cmdSection.getString("permission");
        this.consoleAllowed = cmdSection.getBoolean("console_allowed", true);
        this.override = cmdSection.getBoolean("override", !register);
        this.tab = cmdSection.getBoolean("tab", register);
        execs = new EnumMap<>(ExecType.class);
        loadExecs(cmdSection);
        chains = new TreeSet<>();
        loadArguments(cmdSection);
    }

    private void loadExecs(ConfigurationSection cmdSection) {
        execs.put(ExecType.DEFAULT, cmdSection.getString("exec"));
        execs.put(ExecType.BACKUP, cmdSection.getString("backup"));
        ConfigurationSection errSection = cmdSection.getConfigurationSection("error");
        if (errSection == null) return;
        execs.put(ExecType.ANY_ERROR, errSection.getString("any"));
        execs.put(ExecType.NO_PERMISSIONS, errSection.getString("no_perm"));
        execs.put(ExecType.OFFLINE, errSection.getString("offline"));
        execs.put(ExecType.NOT_INTEGER, errSection.getString("not_int"));
        execs.put(ExecType.NOT_FLOAT, errSection.getString("not_float"));
    }

    private void loadArguments(ConfigurationSection cmdSection) {
        ConfigurationSection argsSection = cmdSection.getConfigurationSection("args");
        if (argsSection == null) return;
        for (String arg : argsSection.getKeys(false))
            chains.add(new ArgumentsChain(arg.toLowerCase(Locale.ROOT), argsSection.getConfigurationSection(arg)));
    }

    /**
     * Get result exec by specific sender and arguments
     *
     * @param sender Sender of command
     * @param args   Used arguments
     * @return Name of result EXEC activator
     */
    public String executeCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            if (!consoleAllowed) {
                return getErroredExec(ExecType.CONSOLE_DISALLOWED);
            }
        } else if (Utils.isRestricted(sender, permission)) return getErroredExec(ExecType.NO_PERMISSIONS);
        if (args.length == 0) {
            return execs.get(ExecType.DEFAULT);
        }
        ExecResult prioritizedResult = null;
        for (ArgumentsChain chain : chains) {
            ExecResult result = chain.executeChain(sender, args);
            ExecType type = result.type();
            String exec = result.exec();
            if (type == ExecType.BACKUP) continue;
            if (type == ExecType.DEFAULT) {
                return exec == null ? execs.getOrDefault(ExecType.DEFAULT, "unknown") : exec;
            } else {
                if (prioritizedResult == null) prioritizedResult = result;
            }
        }
        if (prioritizedResult != null) {
            String exec = prioritizedResult.exec();
            return exec == null ? getErroredExec(prioritizedResult.type()) : exec;
        }
        String backup = execs.get(ExecType.BACKUP);
        return backup == null ? execs.getOrDefault(ExecType.DEFAULT, "unknown") : backup;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String cmd, String[] args, Location loc) {
        List<String> complete = new ArrayList<>();
        if (!tab) return complete;
        if (sender instanceof ConsoleCommandSender && !consoleAllowed) return complete;
        if (Utils.isRestricted(sender, permission)) return complete;
        for (ArgumentsChain chain : chains)
            chain.tabComplete(complete, sender, args);
        return complete;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        executeCommand(sender, args);
        return true;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return ReActions.getPlugin();
    }

    public List<String> list() {
        List<String> list = Utils.getEmptyList(1);
        chains.forEach(c -> list.add(c.toString()));
        return list;
    }

    private String getErroredExec(ExecType type) {
        return Utils.searchNotNull("unknown", execs.get(type), execs.get(ExecType.ANY_ERROR), execs.get(ExecType.BACKUP), execs.get(ExecType.DEFAULT));
    }

    public boolean isOverride() {return this.override;}
}
