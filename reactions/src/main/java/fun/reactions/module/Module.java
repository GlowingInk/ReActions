package fun.reactions.module;

import fun.reactions.ReActions;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.selectors.Selector;
import fun.reactions.util.naming.Named;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface Module extends Named {

    default @NotNull Collection<String> requiredPlugins() {
        return List.of();
    }

    default void preRegister(@NotNull ReActions.Platform platform) { }

    default void postRegister(@NotNull ReActions.Platform platform) { }

    @NotNull Collection<String> getAuthors();

    default @NotNull Collection<ActivatorType> getActivatorTypes() {
        return List.of();
    }

    default @NotNull Collection<Action> getActions() {
        return List.of();
    }

    default @NotNull Collection<Flag> getFlags() {
        return List.of();
    }

    default @NotNull Collection<Placeholder> getPlaceholders() {
        return List.of();
    }

    default @NotNull Collection<Selector> getSelectors() {
        return List.of();
    }
}
