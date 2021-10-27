package me.fromgate.reactions.commands.custom;

import lombok.Getter;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.util.StringUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * One part of arguments chain.
 */
public class Argument {
    private static final List<String> NUMBERS = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

    private final String argument;
    private final Set<String> multiple;
    private final Argument.Type type;

    public Argument(String argument) {
        switch (argument) {
            case "~player":
                this.type = Type.PLAYER;
                multiple = new HashSet<>();
                break;
            case "~int":
                this.type = Type.INTEGER;
                multiple = new HashSet<>();
                break;
            case "~float":
                this.type = Type.FLOAT;
                multiple = new HashSet<>();
                break;
            case "*":
                this.type = Type.ANY;
                multiple = new HashSet<>();
                break;
            default:
                if (argument.contains("|") && !argument.contains("\\|")) {
                    this.type = Type.MULTIPLE_TEXT;
                    multiple = new HashSet<>(List.of(argument.split("\\|")));
                } else {
                    this.type = Type.TEXT;
                    if (argument.startsWith("\\~") || argument.equals("\\*")) argument = argument.substring(1);
                    multiple = Collections.singleton(argument);
                }
        }
        this.argument = argument;
    }

    /**
     * Compare argument for given string
     *
     * @param arg String to check
     * @return {@link ExecType#DEFAULT} if everything is OK, some error if not
     */
    public ExecType check(String arg) {
        return switch (type) {
            case PLAYER -> Bukkit.getPlayerExact(arg) != null ? ExecType.DEFAULT : ExecType.OFFLINE;
            case TEXT -> argument.equalsIgnoreCase(arg) ? ExecType.DEFAULT : ExecType.BACKUP;
            case MULTIPLE_TEXT -> multiple.contains(arg) ? ExecType.DEFAULT : ExecType.BACKUP;
            case INTEGER -> NumberUtils.INT.matcher(arg).matches() ? ExecType.DEFAULT : ExecType.NOT_INTEGER;
            case FLOAT -> NumberUtils.FLOAT.matcher(arg).matches() ? ExecType.DEFAULT : ExecType.NOT_FLOAT;
            default -> ExecType.DEFAULT;
        };
    }

    /**
     * Add value of argument to list of tab-complete if possible
     *
     * @param complete Original list
     * @param arg      Current argument
     */
    public void tabComplete(List<String> complete, String arg) {
        switch (type) {
            case PLAYER -> StringUtil.copyPartialMatches(arg, Utils.getPlayersList(), complete);
            case TEXT, MULTIPLE_TEXT -> StringUtil.copyPartialMatches(arg, multiple, complete);
            case INTEGER, FLOAT -> complete.addAll(NUMBERS);
        }
    }

    /**
     * Get priority of argument based on it's type
     *
     * @return Argument's priority
     */
    public int getPriority() {
        return type.getPriority();
    }

    @Override
    public String toString() {
        return switch (type) {
            case TEXT -> argument;
            case MULTIPLE_TEXT -> String.join(ChatColor.ITALIC + "|" + ChatColor.RESET, multiple);
            default -> ChatColor.ITALIC + argument;
        };
    }

    private enum Type {
        TEXT(10), MULTIPLE_TEXT(8), PLAYER(6), INTEGER(3), FLOAT(4), ANY(1);
        @Getter
        private final int priority;

        Type(int priority) {
            this.priority = priority;
        }
    }
}
