package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholdersManager {
    private static final Pattern PLACEHOLDER_GREEDY = Pattern.compile("(?<!&\\\\)%(\\S+)%");
    private static final Pattern PLACEHOLDER_NONGREEDY = Pattern.compile("(?<!&\\\\)%(\\S+?)%");
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
        } while (!text.equals(oldText) && --limit > 0);

        return PLACEHOLDER_RAW.matcher(text).replaceAll("$1");
    }

    public int getCountLimit() {return this.countLimit;}

    public void setCountLimit(int countLimit) {this.countLimit = countLimit; }

    private static String parseRecursive(String text, Pattern phPattern, RaContext context) {
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
        matcher.appendReplacement(builder, "");
        builder.append(
                Parser.process(
                        parseRecursive(
                                matcher.group(1),
                                pattern,
                                context
                        ),
                        context
                )
        );
    }
}
