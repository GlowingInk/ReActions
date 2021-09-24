package me.fromgate.reactions.module;

import me.fromgate.reactions.logic.activators.ActivatorType;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.selectors.Selector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public interface Module {
    @NotNull String getName();

    @NotNull String @NotNull [] getAuthors();

    default @NotNull Collection<ActivatorType> getActivatorTypes() {
        return Collections.emptyList();
    }

    default @NotNull Collection<Action> getActions() {
        return Collections.emptyList();
    }

    default @NotNull Collection<Flag> getFlags() {
        return Collections.emptyList();
    }

    default @NotNull Collection<Placeholder> getPlaceholders() {
        return Collections.emptyList();
    }

    default @NotNull Collection<Selector> getSelectors() {
        return Collections.emptyList();
    }
}
