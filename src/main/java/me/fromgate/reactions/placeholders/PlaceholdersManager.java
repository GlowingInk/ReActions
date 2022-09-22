package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholdersManager {
    private static final Pattern PLACEHOLDER_GREEDY = Pattern.compile("(?<!&\\\\)%\\S+%");
    private static final Pattern PLACEHOLDER_NONGREEDY = Pattern.compile("(?<!&\\\\)%\\S+?%");
    private static final Pattern PLACEHOLDER_RAW = Pattern.compile("&\\\\(%\\S+%)");

    private int countLimit;

    public PlaceholdersManager() {
        countLimit = 127;
    }

    public void registerPlaceholder(@NotNull Placeholder ph) {
        if (!Parser.Internal.EQUAL.put(ph) && !Parser.Internal.PREFIXED.put(ph)) Parser.Internal.SIMPLE.put(ph);
    }

    public String parsePlaceholders(RaContext context, String text) {
        if (text == null || text.length() < 3) return text;

        String oldText;
        int limit = countLimit;
        do {
            oldText = text;
            text = parseRecursive(text, PLACEHOLDER_GREEDY, context);
            text = parseRecursive(text, PLACEHOLDER_NONGREEDY, context);
        } while (--limit > 0 && !text.equals(oldText));

        return PLACEHOLDER_RAW.matcher(text).replaceAll("$1");
    }

    public int getCountLimit() {return this.countLimit;}

    public void setCountLimit(int countLimit) {this.countLimit = countLimit; }

    private static String parseRecursive(String text, final Pattern phPattern, final RaContext context) {
        Matcher phMatcher = phPattern.matcher(text);
        // If found at least one
        if (phMatcher.find()) {
            StringBuilder builder = new StringBuilder();
            processIteration(builder, phMatcher, phPattern, context);
            while (phMatcher.find()) {
                processIteration(builder, phMatcher, phPattern, context);
            }
            return phMatcher.appendTail(builder).toString();
        }
        return text;
    }

    private static void processIteration(StringBuilder builder, Matcher matcher, Pattern pattern, RaContext context) {
        matcher.appendReplacement(
                builder,
                Matcher.quoteReplacement(
                        Parser.process(
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
