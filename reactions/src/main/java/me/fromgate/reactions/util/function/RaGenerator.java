package me.fromgate.reactions.util.function;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * Used to generate triggers
 *
 * @param <D> Data class for activators creation
 */
@FunctionalInterface
public interface RaGenerator<D> extends BiFunction<ActivatorLogic, D, Activator> {
    /**
     * Generate activator from logic and data container
     *
     * @param logic Logic of activator
     * @param data Data container
     * @return Generated activator, or null if failed
     */
    @Override
    @Nullable Activator apply(@NotNull ActivatorLogic logic, @NotNull D data);
}
