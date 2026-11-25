package fun.reactions.module;

import fun.reactions.ReActions;
import fun.reactions.areas.AreaProvider;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.selectors.Selector;
import fun.reactions.util.item.MetaAspect;
import fun.reactions.util.naming.Named;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface Module extends Named {
    default @NotNull Collection<@NotNull String> requiredPlugins() {
        return List.of();
    }

    default @NotNull Collection<@NotNull String> requiredModules() {
        return List.of();
    }

    default boolean isImportant() {
        return false;
    }

    default void onRegister(@NotNull ReActions.Platform platform) { }

    default void onEnable(@NotNull ReActions.Platform platform) { }

    default void onDisable(@NotNull ReActions.Platform platform) { }

    default @NotNull Collection<@NotNull String> getAuthors() {
        return List.of();
    }

    interface WithActivatorTypes extends Module {
        @NotNull Collection<@NotNull ActivatorType> getActivatorTypes();
    }

    interface WithActions extends Module {
        @NotNull Collection<@NotNull Action> getActions();
    }

    interface WithFlags extends Module {
        @NotNull Collection<@NotNull Flag> getFlags();
    }

    interface WithPlaceholders extends Module {
        @NotNull Collection<@NotNull Placeholder> getPlaceholders();
    }

    interface WithSelectors extends Module {
        @NotNull Collection<@NotNull Selector> getSelectors();
    }

    interface WithAreas extends Module {
        @NotNull Collection<@NotNull AreaProvider> getAreas();
    }

    interface WithItemAspects extends Module {
        @NotNull Collection<@NotNull MetaAspect> getItemAspects();
    }
}
