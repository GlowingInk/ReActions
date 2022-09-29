package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyPlaceholdersManager extends PlaceholdersManager {
    private static final Pattern PLACEHOLDER_GREEDY = Pattern.compile("(?<!&\\\\)%(\\S+)%");
    private static final Pattern PLACEHOLDER_NONGREEDY = Pattern.compile("(?<!&\\\\)%(\\S+?)%");
    private static final Pattern PLACEHOLDER_RAW = Pattern.compile("&\\\\(%\\S+%)");

    public String parsePlaceholders(@NotNull RaContext context, @Nullable String text) {
        if (text == null || text.length() < 3) return text;

        String oldText;
        int limit = countLimit;
        do {
            oldText = text;
            text = parseRecursive(text, PLACEHOLDER_GREEDY, context);
            text = parseRecursive(text, PLACEHOLDER_NONGREEDY, context);
            text = postprocess(context, text);
        } while (!text.equals(oldText) & --limit > 0);

        return PLACEHOLDER_RAW.matcher(text).replaceAll("$1");
    }

    private String parseRecursive(String text, Pattern phPattern, RaContext context) {
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

    private void processIteration(StringBuilder builder, Matcher matcher, Pattern pattern, RaContext context) {
        matcher.appendReplacement(builder, "");
        builder.append(
                resolvePlaceholder(
                        context,
                        parseRecursive(
                                matcher.group(1),
                                pattern,
                                context
                        )
                )
        );
    }
}
