package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.ModernPlaceholdersManager;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import ink.glowing.text.InkyMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

@Aliased.Names({"escape-inky", "escape-params", "escape-params-full", "escape-phs"})
public class EscapePlaceholder implements Placeholder {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params) {
        Set<Type> escapeTypes;
        String phText = params;
        switch (key) {
            case "escape-inky" -> escapeTypes = Set.of(Type.INKY);
            case "escape-params" -> escapeTypes = Set.of(Type.PARAMETERS);
            case "escape-params-full" -> escapeTypes = Set.of(Type.PARAMETERS_FULL);
            case "escape-phs" -> escapeTypes = Set.of(Type.PLACEHOLDERS);
            default -> {
                String[] split = params.split("\\|", 2);
                if (split.length == 1) {
                    escapeTypes = new TreeSet<>();
                    escapeTypes.add(Type.PLACEHOLDERS);
                    escapeTypes.add(Type.PARAMETERS);
                } else {
                    String[] typesStr = split[0].split(",");
                    escapeTypes = new TreeSet<>();
                    for (String typeStr : typesStr) switch (typeStr.toLowerCase(Locale.ROOT)) {
                        case "inky", "inkymessage" -> escapeTypes.add(Type.INKY);
                        case "params", "parameters" -> escapeTypes.add(Type.PARAMETERS);
                        case "params-full", "parameters-full" -> escapeTypes.add(Type.PARAMETERS_FULL);
                        case "phs", "placeholders" -> escapeTypes.add(Type.PLACEHOLDERS);
                    }
                    phText = split[1];
                }
            }
        }
        String resolved = env.getPlatform().getPlaceholders().resolvePlaceholder(
                new Environment(
                        env.getPlatform(),
                        "",
                        env.getVariables(),
                        env.getPlayer(),
                        env.getDepth() + 1,
                        env.isAsync()
                ), phText
        );
        if (resolved == null) return null;
        for (Type escapeType : escapeTypes) {
            resolved = switch (escapeType) {
                case INKY -> InkyMessage.escape(resolved);
                case PARAMETERS -> Parameters.escapeValue(resolved);
                case PARAMETERS_FULL -> Parameters.escape(resolved);
                case PLACEHOLDERS -> ModernPlaceholdersManager.escape(resolved);
            };
        }
        return resolved;
    }

    @Override
    public @NotNull String getName() {
        return "escape";
    }

    private enum Type {
        PARAMETERS_FULL, PARAMETERS, INKY, PLACEHOLDERS
    }
}
