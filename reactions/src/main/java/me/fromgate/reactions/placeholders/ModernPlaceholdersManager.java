package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModernPlaceholdersManager extends PlaceholdersManager {
    private static final Pattern PRE_ESCAPE = Pattern.compile("(\\\\+)(?!%\\[|[\\\\\\]])"); // "(\\+)(?!%\[|[\\\]])"

    @Override
    public @NotNull String parsePlaceholders(@NotNull Environment env, @NotNull String text) {
        if (text.length() < 4) return text;
        text = preEscape(text);
        String oldText;
        int limit = countLimit;
        do {
            oldText = text;
            text = resolvePreprocess(env, text);
            text = parse(env, text);
        } while (!oldText.equals(text) & --limit > 0);
        return unescapeSpecial(text);
    }

    private String parse(@NotNull Environment env, @NotNull String text) {
        StringBuilder builder = new StringBuilder(text.length());
        IterationStage stage = IterationStage.TEXT;
        int stepIndex = 0;
        boolean allowSpecial = true;
        for (int index = 0; index < text.length(); index++) {
            char c = text.charAt(index);
            if (c == '\\' && stage != IterationStage.PLACEHOLDER_CHECK) {
                if (stage == IterationStage.TEXT) {
                    builder.append(text, stepIndex, index + 1);
                    stepIndex = index + 1;
                }
                allowSpecial = false;
                if (text.length() == ++index) {
                    break;
                }
                c = text.charAt(index);
            }
            switch (stage) {
                case TEXT -> {
                    if (c == '%') {
                        if (allowSpecial) {
                            stage = IterationStage.PLACEHOLDER_CHECK;
                        }
                    }
                }
                case PLACEHOLDER_CHECK -> {
                    if (c == '[') {
                        builder.append(text, stepIndex, index - 1);
                        stepIndex = index - 1;
                        stage = IterationStage.PLACEHOLDER_INSIDE;
                    } else {
                        stage = IterationStage.TEXT;
                    }
                }
                case PLACEHOLDER_INSIDE -> {
                    if (c == ' ') {
                        stage = IterationStage.TEXT;
                    } else if (allowSpecial) {
                        if (c == ']') {
                            String substring = text.substring(stepIndex + 2, index);
                            String processed = resolvePlaceholder(env, substring);
                            if (processed != null) {
                                if (text.length() > index + 3 && text.charAt(index + 1) == '(') {
                                    String options = optionsSearch(text, index + 2);
                                    if (options != null) {
                                        index += options.length() + 2;
                                        if (options.contains("prms")) processed = Parameters.escapeParameters(processed);
                                        if (options.contains("phs")) processed = escapeSpecial(processed);
                                    }
                                }
                                builder.append(processed);
                            } else {
                                builder.append("\\%[").append(substring).append("\\]");
                            }
                            stepIndex = index + 1;
                            stage = IterationStage.TEXT;
                        } else if (c == '%') {
                            stage = IterationStage.PLACEHOLDER_CHECK;
                        }
                    }
                }
            }
            allowSpecial = true;
        }
        if (stepIndex != text.length()) {
            builder.append(text, stepIndex, text.length());
        }
        return builder.toString();
    }

    private static @Nullable String optionsSearch(@NotNull String text, int startIndex) {
        for (int index = startIndex; index < text.length(); ++index) {
            char c = text.charAt(index);
            if (c == ')') {
                return text.substring(startIndex, index);
            } else if ((c > 'z' || c < 'a') && c != ',') {
                return null;
            }
        }
        return null;
    }

    private static @NotNull String preEscape(@NotNull String text) {
        StringBuilder builder = new StringBuilder(text.length());
        Matcher matcher = PRE_ESCAPE.matcher(text);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(builder, "");
            builder.append(group).append(group);
        }
        return matcher.appendTail(builder).toString();
    }

    private static @NotNull String escapeSpecial(@NotNull String text) {
        return text
                .replace("\\", "\\\\")
                .replace("%[", "\\%[")
                .replace("]", "\\]");
    }

    private static @NotNull String unescapeSpecial(@NotNull String text) {
        return text
                .replace("\\\\", "\\")
                .replace("\\%[", "%[")
                .replace("\\]", "]");
    }

    private enum IterationStage {
        TEXT, PLACEHOLDER_CHECK, PLACEHOLDER_INSIDE
    }
}
