package fun.reactions.model.activators.type;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.Activator;
import fun.reactions.util.function.RaGenerator;
import fun.reactions.util.naming.Named;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO Some kind of activator scheme
public interface ActivatorType extends Named {
    static @NotNull ActivatorType of(@NotNull Class<? extends Activator> type, @NotNull String name, @NotNull RaGenerator<Parameters> creator) {
        return of(type, name, creator, false);
    }

    static @NotNull ActivatorType of(@NotNull Class<? extends Activator> type, @NotNull String name, @NotNull RaGenerator<Parameters> creator, boolean needBlock) {
        return new Simple(type, name, creator, needBlock);
    }

    /**
     * Basically an ID of activator type
     * @return name of activator type
     */
    @Override
    @NotNull String getName();

    // TODO Module name?

    /**
     * Get producing Activator class of this ActivatorType
     * @return producing class
     */
    @NotNull Class<? extends Activator> getActivatorClass();

    /**
     * Create a new Activator from given base and parameters
     * @param logic base of activator
     * @param params parameters of activator
     * @return a new Activator, or null, if given parameters are invalid
     */
    @Nullable Activator createActivator(@NotNull Logic logic, @NotNull Parameters params);

    /**
     * Load an Activator from given base and config
     * @param logic base of activator
     * @param cfg config of activator
     * @return an Activator, or null, if given config is invalid
     */
    default @Nullable Activator loadActivator(@NotNull Logic logic, @NotNull ConfigurationSection cfg) {
        return createActivator(logic, Parameters.fromConfiguration(cfg, Logic::isReservedKey));
    }

    default void include(@NotNull Activator activator) { }

    default void discard(@NotNull Activator activator) { }

    boolean isNeedBlock();

    class Simple implements ActivatorType {
        private final Class<? extends Activator> type;
        private final RaGenerator<Parameters> creator;
        private final boolean needBlock;
        private final String name;

        private Simple(Class<? extends Activator> type, String name, RaGenerator<Parameters> creator, boolean needBlock) {
            this.type = type;
            this.creator = creator;
            this.needBlock = needBlock;
            this.name = name;
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
        public Activator createActivator(@NotNull Logic logic, @NotNull Parameters params) {
            return creator.apply(logic, params);
        }

        @Override
        public boolean isNeedBlock() {
            return needBlock;
        }
    }
}
