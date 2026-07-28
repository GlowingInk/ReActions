package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import fun.reactions.util.parameter.Parameterizable;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class TrackedVariable implements Variable, Parameterizable {
    private boolean changed;

    @Override
    public void markChanged() {
        changed = true;
    }

    @Override
    public @NotNull Optional<String> changed() {
        return changed ? Optional.of(get()) : Optional.empty();
    }

    @Override
    public @NotNull Parameters asParameters() {
        return Variable.super.asParameters();
    }
}
