package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.ModernPlaceholdersManager;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import ink.glowing.text.InkyMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Aliased.Names("esc")
public class EscapePlaceholder implements Placeholder {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params) {
        Set<EscapeType> escapeTypes;
        String phText = params;
        String[] split = params.split("\\|", 2);
        escapeTypes = new TreeSet<>();
        if (split.length == 1) {
            escapeTypes.add(EscapeType.PLACEHOLDERS);
            escapeTypes.add(EscapeType.PARAMETERS);
        } else {
            String[] typesStr = split[0].split(",");
            for (String typeStr : typesStr) {
                EscapeType escapeType = EscapeType.TYPES_MAP.get(typeStr.toLowerCase(Locale.ROOT));
                if (escapeType != null) escapeTypes.add(escapeType);
            }
            phText = split[1];
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
        for (EscapeType escapeType : escapeTypes) {
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

    private enum EscapeType {
        PARAMETERS_FULL(
                "params-full", "param-full", "prms-full", "prm-full"
        ),
        PARAMETERS(
                "params", "param", "prms", "prm"
        ),
        INKY(
                "inkymessage", "inkymsg", "im"
        ),
        PLACEHOLDERS(
                "placeholder", "phs", "ph"
        );

        private static final Map<String, EscapeType> TYPES_MAP;
        static {
            Map<String, EscapeType> typesMap = new HashMap<>();
            for (EscapeType type : values()) {
                typesMap.put(type.name().toLowerCase(Locale.ROOT), type);
                for (String alias : type.aliases) {
                    typesMap.put(alias, type);
                }
            }
            TYPES_MAP = Map.copyOf(typesMap);
        }

        private final List<String> aliases;

        EscapeType(String... aliases) {
            this.aliases = List.of(aliases);
        }
    }
}
