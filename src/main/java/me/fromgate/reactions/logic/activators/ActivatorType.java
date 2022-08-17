package me.fromgate.reactions.logic.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ActivatorType {
    @NotNull
    Class<? extends Activator> getType();

    @NotNull
    String getName();

    @NotNull
    Collection<Activator> getActivators();

    @Nullable
    Activator createActivator(@NotNull ActivatorLogic logic, @NotNull Parameters cfg);

    @Nullable
    Activator loadActivator(@NotNull ActivatorLogic logic, @NotNull ConfigurationSection cfg);

    // TODO Module name?

    boolean isNeedBlock();

    void addActivator(@NotNull Activator activator);

    void removeActivator(@NotNull Activator activator);

    void clearActivators();

    void activate(@NotNull Storage storage);

    boolean isEmpty();
}
