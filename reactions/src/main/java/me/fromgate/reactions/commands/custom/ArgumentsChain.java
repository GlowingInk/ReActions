package me.fromgate.reactions.commands.custom;

import me.fromgate.reactions.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * One chain of arguments
 */
public class ArgumentsChain implements Comparable<ArgumentsChain> {
    // TODO: Prebuild ExecResults maybe?
    private final Map<ExecType, String> execs;
    private final List<Argument> arguments;
    private final String permission;
    private final boolean consoleAllowed;
    private final boolean ignoreAfter;
    private final int priority;

    public ArgumentsChain(String chain, ConfigurationSection argSection) {
        execs = new EnumMap<>(ExecType.class);
        loadExecs(argSection);
        arguments = new ArrayList<>();
        consoleAllowed = argSection.getBoolean("console_allowed", true);
        ignoreAfter = argSection.getBoolean("ignore_after", true);
        permission = argSection.getString("permission");
        int priority = 0;
        for (String arg : chain.split("\\s")) {
            Argument argument = new Argument(arg);
            priority += argument.getPriority();
            arguments.add(argument);
        }
        this.priority = priority;
    }

    private void loadExecs(ConfigurationSection argSection) {
        if (argSection == null) return;
        execs.put(ExecType.DEFAULT, argSection.getString("exec"));
        ConfigurationSection errSection = argSection.getConfigurationSection("error");
        if (errSection == null) return;
        execs.put(ExecType.ANY_ERROR, errSection.getString("any"));
        execs.put(ExecType.NO_PERMISSIONS, errSection.getString("no_perm"));
        execs.put(ExecType.OFFLINE, errSection.getString("offline"));
        execs.put(ExecType.NOT_INTEGER, errSection.getString("not_int"));
        execs.put(ExecType.NOT_FLOAT, errSection.getString("not_float"));
    }

    /**
     * Add value of n argument to list of tab-complete if possible
     *
     * @param complete Original list
     * @param sender   Sender of request to tab-complete
     * @param args     Current arguments
     */
    public void tabComplete(List<String> complete, CommandSender sender, String[] args) {
        if (args.length > arguments.size()) return;

        if (sender instanceof ConsoleCommandSender) {
            if (!consoleAllowed) return;
        } else if (Utils.isRestricted(sender, permission)) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            if (i != args.length - 1) {
                if (arguments.get(i).check(args[i]) == ExecType.BACKUP)
                    return;
            } else {
                arguments.get(i).tabComplete(complete, args[i]);
            }
        }
    }

    /**
     * Get amount of arguments in chain
     *
     * @return Amount of arguments
     */
    public int size() {
        return arguments.size();
    }

    /**
     * Try to use arguments chain
     *
     * @param sender Sender of the command
     * @param args   Array of arguments
     * @return Result of command
     */
    public ExecResult executeChain(CommandSender sender, List<String> args) {
        if (args.size() < arguments.size() || (!ignoreAfter && args.size() > arguments.size())) {
            return ExecResult.BLANK_BACKUP;
        }

        if (!(sender instanceof Player)) {
            if (!consoleAllowed) {
                return new ExecResult(ExecType.CONSOLE_DISALLOWED, getErroredExec(ExecType.CONSOLE_DISALLOWED));
            }
        } else if (Utils.isRestricted(sender, permission)) {
            return new ExecResult(ExecType.NO_PERMISSIONS, getErroredExec(ExecType.NO_PERMISSIONS));
        }

        for (int i = 0; i < arguments.size(); i++) {
            ExecType resultType = arguments.get(i).check(args.get(i).toLowerCase(Locale.ROOT));
            if (resultType != ExecType.DEFAULT) {
                return new ExecResult(resultType, getErroredExec(resultType));
            }
            if (i == arguments.size() - 1) {
                return new ExecResult(ExecType.DEFAULT, execs.get(ExecType.DEFAULT));
            }
        }

        return ExecResult.BLANK_BACKUP;
    }

    private String getErroredExec(ExecType type) {
        return Utils.searchNotNull(execs.get(ExecType.ANY_ERROR), execs.get(type));
    }

    @Override
    public int compareTo(ArgumentsChain obj) {
        return this.priority == obj.priority ? 1 : -1;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        arguments.forEach(a -> str.append(a).append(ChatColor.RESET).append(" "));
        return str.toString();
    }
}
