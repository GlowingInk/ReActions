package fun.reactions.module.basics.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author MaxDikiy
 * @since 29/04/2017
 */
public class RegexAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        String prefix = params.getString("prefix");
        String regex = params.getString("regex");
        String input = params.getString("input", removeParams(env, params.origin()));

        if (input.isEmpty()) return false;

        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(input);
        int count = -1;
        String group;

        while (m.find()) {
            count++;
            for (int i = 0; i <= m.groupCount(); i++) {
                if (m.group(i) != null) group = m.group(i);
                else group = "";
                env.getVariables().set(prefix + "group" + count + "" + i, group);
                env.getVariables().set(prefix + "group_" + count + "_" + i, group);
                env.getVariables().set(prefix + "group:" + count + ":" + i, group);
            }
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "REGEX";
    }

    // TODO: Remove it somehow
    private String removeParams(Environment env, String message) {
        String sb = "(?i)(" + String.join("|", env.getPlatform().getSelectors().getAllKeys()) +
                "|hide|regex|prefix):(\\{.*}|\\S+)\\s?";
        return message.replaceAll(sb, "");
    }

}