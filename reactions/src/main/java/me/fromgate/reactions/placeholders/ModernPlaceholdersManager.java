package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModernPlaceholdersManager extends PlaceholdersManager {
    private static final Pattern SLASH = Pattern.compile("\\\\\\\\|\\\\");

    @Override
    public String parsePlaceholders(@NotNull RaContext context, @Nullable String text) {
        if (text == null || text.length() < 4) return text;
        text = escapeSlash(text);
        String oldText;
        int limit = countLimit;
        do {
            oldText = text;
            text = parse(context, text);
            text = postprocess(context, text);
        } while (!oldText.equals(text) & --limit > 0);
        return unescapeSlash(text);
    }

    private String parse(RaContext context, String text) {
        StringBuilder builder = new StringBuilder(text.length());
        IterationStage stage = IterationStage.TEXT;
        int stepIndex = 0;
        boolean allowSpecial = false;
        boolean recursive = false;
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
                            recursive = true;
                            stage = IterationStage.PLACEHOLDER_CHECK;
                        }
                    } else if (c == '#') {
                        if (allowSpecial) {
                            recursive = false;
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
                    } else if (c == ']') {
                        if (allowSpecial) {
                            String substring = text.substring(stepIndex + 2, index);
                            String processed = resolvePlaceholder(context, substring);
                            if (processed != null) {
                                builder.append(recursive ? processed : escapeFully(processed));
                            } else {
                                builder.append(recursive ? "\\%[" : "\\#[").append(substring).append("\\]");
                            }
                            stepIndex = index + 1;
                            stage = IterationStage.TEXT;
                        }
                    } else if (c == '%') {
                        if (allowSpecial) {
                            recursive = true;
                            stage = IterationStage.PLACEHOLDER_CHECK;
                        }
                    } else if (c == '#') {
                        if (allowSpecial) {
                            recursive = false;
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

    enum IterationStage {
        TEXT, PLACEHOLDER_CHECK, PLACEHOLDER_INSIDE
    }

    private static String escapeSlash(String text) {
        return text.replace("\\", "\\\\\\");
    }

    private static String escapeFully(String text) {
        return escapeSlash(text)
                .replace("%[", "\\%[")
                .replace("#[", "\\#[")
                .replace("]", "\\]");
    }

    private static String unescapeSlash(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        Matcher matcher = SLASH.matcher(text);
        while (matcher.find()) {
            matcher.appendReplacement(builder, "");
            String group = matcher.group();
            if (group.equals("\\\\")) {
                builder.append('\\');
            }
        }
        return matcher.appendTail(builder).toString();
    }
}
