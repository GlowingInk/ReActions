package me.fromgate.reactions.logic.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ActivatorType {
    /**
     * Basically an ID of activator type
     * @return name of activator type
     */
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
    @Nullable Activator createActivator(@NotNull ActivatorLogic logic, @NotNull Parameters params);

    /**
     * Load an Activator from given base and config
     * @param logic base of activator
     * @param cfg config of activator
     * @return an Activator, or null, if given config is invalid
     */
    @Nullable Activator loadActivator(@NotNull ActivatorLogic logic, @NotNull ConfigurationSection cfg);

    // v TODO That's probably a bad design, ActivatorType should not handle activators storing?

    /**
     * Get a live collection of registered activators
     * @return registered activators
     */
    @NotNull Collection<Activator> getActivators();

    /**
     * Is collection returned by {@link ActivatorType#getActivators()} is empty
     * @return is underlying collection is empty
     */
    boolean isEmpty();

    void addActivator(@NotNull Activator activator);

    void removeActivator(@NotNull Activator activator);

    void clearActivators();

    void activate(@NotNull Storage storage);

    // ^ TODO

    boolean isNeedBlock();
}
