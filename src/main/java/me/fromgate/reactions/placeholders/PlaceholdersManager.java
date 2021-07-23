package me.fromgate.reactions.placeholders;

import lombok.Getter;
import lombok.Setter;
import me.fromgate.reactions.module.basics.flags.Flags;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholdersManager {
    private static final Pattern PLACEHOLDER_GREEDY = Pattern.compile("(?<!&\\\\)%\\S+%");
    private static final Pattern PLACEHOLDER_NONGREEDY = Pattern.compile("(?<!&\\\\)%\\S+?%");
    private static final Pattern PLACEHOLDER_RAW = Pattern.compile("&\\\\(%\\S+%)");

    @Getter
    @Setter
    private int countLimit = 127;

    public PlaceholdersManager() {
        register(new PlaceholderPlayer());
        register(new PlaceholderMoney());
        register(new PlaceholderRandom());
        register(new PlaceholderTime());
        register(new PlaceholderCalc());
        register(new PlaceholderActivator());
        register(new PlaceholderVariable());
        register((c,p,t) -> c.getVariable(p)); // Temporary variables
        register(new PlaceholderPAPI());
    }

    public boolean register(Placeholder ph) {
        if(ph == null) return false;
        return InternalParsers.EQUAL.put(ph) || InternalParsers.PREFIXED.put(ph) || InternalParsers.SIMPLE.put(ph);
    }

    public String parsePlaceholders(RaContext context, String text) {
        if(text == null || text.length() < 3) return text;

        String oldText;
        int limit = countLimit;
        do {
            oldText = text;
            text = parseRecursive(text, PLACEHOLDER_GREEDY, context);
            text = parseRecursive(text, PLACEHOLDER_NONGREEDY, context);
        } while(--limit > 0 && !text.equals(oldText));

        return PLACEHOLDER_RAW.matcher(text).replaceAll("$1");
    }

    private static String parseRecursive(String text, final Pattern phPattern, final RaContext context) {
        Matcher phMatcher = phPattern.matcher(text);
        // If found at least one
        if(phMatcher.find()) {
            StringBuffer buf = new StringBuffer();
            processIteration(buf, phMatcher, phPattern, context);
            while(phMatcher.find()) {
                processIteration(buf, phMatcher, phPattern, context);
            }
            return phMatcher.appendTail(buf).toString();
        }
        return text;
    }

    // Just some sh!tty stuff
    private static void processIteration(StringBuffer buffer, Matcher matcher, Pattern pattern, RaContext context) {
        matcher.appendReplacement(
                buffer,
                Matcher.quoteReplacement(
                        InternalParsers.process(
                                parseRecursive(
                                        crop(matcher.group()),
                                        pattern,
                                        context
                                ),
                                context
                        )
                )
        );
    }

    private static String crop(String text) {
        return text.substring(1, text.length() - 1);
    }

    // TODO: That's terrible. Better to rewrite help system...
    public void listPlaceholders(CommandSender sender, int pageNum) {
        List<String> phList = new ArrayList<>();
        for (Placeholder ph : InternalParsers.EQUAL.getPlaceholders()) {
            phList.add(((Placeholder.Equal)ph).getId());
        }
        for (Placeholder ph : InternalParsers.PREFIXED.getPlaceholders()) {
            phList.add(((Placeholder.Equal)ph).getId());
        }

        phList.replaceAll(s -> {
            Msg desc = Msg.getByName("placeholder_" + s);
            if (desc == null) {
                Msg.LNG_FAIL_PLACEHOLDER_DESC.log(s);
                return s;
            } else {
                return "&6" + s + "&3: &a" + desc.getText("NOCOLOR");
            }
        });

        for (Flags f : Flags.values()) {
            if (f != Flags.TIME && f != Flags.CHANCE) continue;
            String name = f.name();
            Msg desc = Msg.getByName("placeholder_" + name);
            if (desc == null) {
                Msg.LNG_FAIL_PLACEHOLDER_DESC.log(name);
            } else {
                phList.add("&6" + name + "&3: &a" + desc.getText("NOCOLOR"));
            }
        }
        phList.add("&6VAR&3: &a" + Msg.PLACEHOLDER_VAR.getText("NOCOLOR"));
        phList.add("&6SIGN_LOC, SIGN_LINE1,.. SIGN_LINE4&3: &a" + Msg.PLACEHOLDER_SIGNACT.getText("NOCOLOR"));
        phList.add("&6ARG0, ARG1, ARG2...&3: &a" + Msg.PLACEHOLDER_COMMANDACT.getText("NOCOLOR"));
        Msg.printPage(sender, phList, Msg.MSG_PLACEHOLDERLISTTITLE, pageNum, sender instanceof Player ? 10 : 1000);
    }
}
