package me.fromgate.reactions.placeholders;

import lombok.Getter;
import lombok.Setter;
import me.fromgate.reactions.util.data.RaContext;

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
}
