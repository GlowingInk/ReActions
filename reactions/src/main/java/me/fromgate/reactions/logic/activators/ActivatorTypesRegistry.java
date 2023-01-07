package me.fromgate.reactions.logic.activators;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.util.function.RaGenerator;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
            throw new IllegalStateException("Activator type '" + type.getName() + "' is already registered!");
        }
        String name = type.getName().toUpperCase(Locale.ROOT);
        if (typesAliases.containsKey(name)) {
            ActivatorType preserved = typesAliases.get(name);
            if (preserved.getName().equalsIgnoreCase(name)) {
                throw new IllegalStateException("Activator type name '" + name + "' is already used for '" + preserved.getName() + "'!");
            } else {
                logger.warn("Activator type name '" + name + "' is already used as an alias for '" + preserved.getName() + "', overriding it.");
            }
        }
        typesAliases.put(name, type);
        types.put(type.getActivatorClass(), type);
        Collection<String> aliases = Aliased.getAliases(type);
        if (aliases.isEmpty()) {
            aliases = Aliased.getAliases(type.getActivatorClass());
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

    public static @NotNull ActivatorType typeOf(@NotNull Class<? extends Activator> type, @NotNull String name, @NotNull RaGenerator<Parameters> creator, @NotNull RaGenerator<ConfigurationSection> loader) {
        return typeOf(type, name, creator, loader, false);
    }

    public static @NotNull ActivatorType typeOf(@NotNull Class<? extends Activator> type, @NotNull String name, @NotNull RaGenerator<Parameters> creator, @NotNull RaGenerator<ConfigurationSection> loader, boolean needBlock) {
        return new SimpleType(type, name, creator, loader, needBlock);
    }

    private static class SimpleType implements ActivatorType {
        private final Class<? extends Activator> type;
        private final RaGenerator<Parameters> creator;
        private final RaGenerator<ConfigurationSection> loader;
        private final boolean needBlock;
        private final String name;
        private final Set<Activator> activators;

        public SimpleType(Class<? extends Activator> type, String name, RaGenerator<Parameters> creator, RaGenerator<ConfigurationSection> loader, boolean needBlock) {
            this.type = type;
            this.creator = creator;
            this.loader = loader;
            this.needBlock = needBlock;
            this.name = name;
            this.activators = new HashSet<>();
        }

        @Override
        public @NotNull Class<? extends Activator> getActivatorClass() {
            return type;
        }

        @Override
        public @NotNull String getName() {
            return name;
        }

        @Override
        public @NotNull Set<Activator> getActivators() {
            return activators;
        }

        @Override
        public Activator createActivator(@NotNull ActivatorLogic logic, @NotNull Parameters params) {
            return creator.apply(logic, params);
        }

        @Override
        public Activator loadActivator(@NotNull ActivatorLogic logic, @NotNull ConfigurationSection cfg) {
            return loader.apply(logic, cfg);
        }

        @Override
        public boolean isNeedBlock() {
            return needBlock;
        }

        @Override
        public void addActivator(@NotNull Activator activator) {
            activators.add(activator);
        }

        @Override
        public void removeActivator(@NotNull Activator activator) {
            activators.remove(activator);
        }

        @Override
        public void clearActivators() {
            activators.clear();
        }

        @Override
        public void activate(@NotNull Storage storage) {
            for (Activator activator : getActivators()) {
                activator.executeActivator(storage);
            }
        }

        @Override
        public boolean isEmpty() {
            return activators.isEmpty();
        }
    }
}
