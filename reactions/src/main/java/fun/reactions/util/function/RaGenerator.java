package fun.reactions.util.function;

import fun.reactions.logic.Logic;
import fun.reactions.logic.activators.Activator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * Used to generate triggers
 *
 * @param <D> Data class for activators creation
 */
@FunctionalInterface
public interface RaGenerator<D> extends BiFunction<Logic, D, Activator> {
    /**
     * Generate activator from logic and data container
     *
     * @param logic Logic of activator
     * @param data Data container
     * @return Generated activator, or null if failed
     */
    @Override
    @Nullable Activator apply(@NotNull Logic logic, @NotNull D data);
}
