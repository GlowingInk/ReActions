package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.context.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyPlaceholdersManager extends PlaceholdersManager {
    private static final Pattern PLACEHOLDER_GREEDY = Pattern.compile("(?<!&\\\\)%(\\S+)%");
    private static final Pattern PLACEHOLDER_NONGREEDY = Pattern.compile("(?<!&\\\\)%(\\S+?)%");
    private static final Pattern PLACEHOLDER_RAW = Pattern.compile("&\\\\(%\\S+%)");

    @Override
    public @NotNull String parsePlaceholders(@NotNull Environment context, @NotNull String text) {
        if (text.length() < 3) return text;

        String oldText;
        int limit = countLimit;
        do {
            oldText = text;
            text = resolvePreprocess(context, text);
            text = parseRecursive(text, PLACEHOLDER_GREEDY, context);
            text = parseRecursive(text, PLACEHOLDER_NONGREEDY, context);
        } while (!text.equals(oldText) & --limit > 0);

        return PLACEHOLDER_RAW.matcher(text).replaceAll("$1");
    }

    private @NotNull String parseRecursive(@NotNull String text, @NotNull Pattern phPattern, @NotNull Environment context) {
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

    private void processIteration(@NotNull StringBuilder builder, @NotNull Matcher matcher, @NotNull Pattern pattern, @NotNull Environment context) {
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
