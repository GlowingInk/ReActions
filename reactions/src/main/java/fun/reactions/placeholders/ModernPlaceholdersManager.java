package fun.reactions.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModernPlaceholdersManager extends PlaceholdersManager {
    private static final Pattern PRE_ESCAPE = Pattern.compile("(\\\\+)(?!%\\[|[\\\\\\]])"); // "(\\+)(?!%\[|[\\\]])"

    @Override
    public @NotNull String parse(@NotNull Environment env, @NotNull String text) {
        if (text.length() < 4) return text;
        text = preEscape(text);
        String oldText;
        int limit = countLimit;
        do {
            oldText = text;
            text = parseGradually(env, text);
        } while (!oldText.equals(text) && --limit > 0);
        return unescape(text);
    }

    private String parseGradually(@NotNull Environment env, @NotNull String text) {
        StringBuilder builder = new StringBuilder(text.length());
        IterationStage stage = IterationStage.TEXT;
        int stepIndex = 0;
        boolean allowSpecial = true;
        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);
            if (ch == '\\' && stage != IterationStage.PLACEHOLDER_CHECK) {
                if (stage == IterationStage.TEXT) {
                    builder.append(text, stepIndex, index + 1);
                    stepIndex = index + 1;
                }
                allowSpecial = false;
                if (text.length() == ++index) {
                    break;
                }
                ch = text.charAt(index);
            }
            switch (stage) {
                case TEXT -> {
                    if (ch == '%' && allowSpecial) {
                        stage = IterationStage.PLACEHOLDER_CHECK;
                    }
                }
                case PLACEHOLDER_CHECK -> {
                    if (ch == '[') {
                        builder.append(text, stepIndex, index - 1);
                        stepIndex = index - 1;
                        stage = IterationStage.PLACEHOLDER_INSIDE;
                    } else {
                        stage = IterationStage.TEXT;
                    }
                }
                case PLACEHOLDER_INSIDE -> {
                    if (ch == ' ') {
                        stage = IterationStage.TEXT;
                    } else if (allowSpecial) {
                        if (ch == ']') {
                            String substring = text.substring(stepIndex + 2, index);
                            String processed = resolvePlaceholder(env, substring);
                            if (processed != null) {
                                if (text.length() > index + 3 && text.charAt(index + 1) == '(') {
                                    String options = optionsSearch(text, index + 2);
                                    if (options != null) {
                                        env.warn(
                                                "Usage of %[placeholder](...) is not supported anymore. " +
                                                "Consider using %[escape:...|placeholder] instead"
                                        );
                                        index += options.length() + 2;
                                        if (options.contains("prms")) processed = Parameters.escapeValue(processed);
                                        if (options.contains("phs")) processed = escape(processed);
                                    }
                                }
                                builder.append(processed);
                            } else {
                                builder.append("\\%[").append(substring).append("\\]");
                            }
                            stepIndex = index + 1;
                            stage = IterationStage.TEXT;
                        } else if (ch == '%') {
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

    public static @NotNull String escape(@NotNull String text) {
        return text
                .replace("\\", "\\\\")
                .replace("%[", "\\%[")
                .replace("]", "\\]");
    }

    public static @NotNull String unescape(@NotNull String text) {
        return text
                .replace("\\\\", "\\")
                .replace("\\%[", "%[")
                .replace("\\]", "]");
    }

    private enum IterationStage {
        TEXT, PLACEHOLDER_CHECK, PLACEHOLDER_INSIDE
    }
}
