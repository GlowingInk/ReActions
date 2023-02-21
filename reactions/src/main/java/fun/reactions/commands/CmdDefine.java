package fun.reactions.commands;

import fun.reactions.util.message.Msg;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CmdDefine {
    String command();

    String[] subCommands();

    String permission();

    boolean allowConsole() default false;

    Msg description();

    String shortDescription();
}

