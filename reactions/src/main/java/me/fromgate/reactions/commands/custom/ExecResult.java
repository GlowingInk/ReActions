package me.fromgate.reactions.commands.custom;

public record ExecResult(ExecType type, String exec) {
    public static final ExecResult BLANK_BACKUP = new ExecResult(ExecType.BACKUP, null);
}
