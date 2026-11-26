package fun.reactions.model.activators.type;

import fun.reactions.ReActions;
import fun.reactions.model.activators.Activator;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.util.*;

public class ActivatorTypesRegistry {
    private final Logger logger;
    private final Map<Class<? extends Activator>, ActivatorType> types;
    private final Map<String, ActivatorType> typesAliases;

    public ActivatorTypesRegistry(ReActions.Platform platform) {
        logger = platform.logger();
        types = new HashMap<>();
        typesAliases = new HashMap<>();
    }

    public void registerType(@NotNull ActivatorType type) {
        if (types.containsKey(type.getActivatorClass())) {
            throw new IllegalStateException("Activator type '" + type.getName() + "' is already registered");
        }
        String name = type.getName().toUpperCase(Locale.ROOT);
        if (typesAliases.containsKey(name)) {
            ActivatorType preserved = typesAliases.get(name);
            if (preserved.getName().equalsIgnoreCase(name)) {
                throw new IllegalStateException("Activator type name '" + name + "' is already used for '" + preserved.getName() + "'");
            } else {
                logger.warn("Activator type name '{}' is already used as an alias for '{}', overriding it.", name, preserved.getName());
            }
        }
        typesAliases.put(name, type);
        types.put(type.getActivatorClass(), type);
        Collection<String> aliases = Aliased.getAliasesOf(type);
        if (aliases.isEmpty()) {
            aliases = Aliased.getAliasesOf(type.getActivatorClass());
        }
        for (String alias : aliases) {
            typesAliases.putIfAbsent(alias.toUpperCase(Locale.ROOT), type);
        }
    }

    public boolean contains(@NotNull String name) {
        return typesAliases.containsKey(name);
    }

    public @Nullable ActivatorType get(@NotNull String name) {
        return typesAliases.get(name.toUpperCase(Locale.ROOT));
    }

    public @Nullable ActivatorType get(@NotNull Class<? extends Activator> type) {
        return types.get(type);
    }

    public @NotNull @UnmodifiableView Collection<ActivatorType> getTypes() {
        return Collections.unmodifiableCollection(types.values());
    }

    public @NotNull @UnmodifiableView Collection<String> getTypeNames() {
        return Collections.unmodifiableCollection(typesAliases.keySet());
    }
}
