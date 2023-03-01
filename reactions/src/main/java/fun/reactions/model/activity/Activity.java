package fun.reactions.model.activity;

import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Named;
import fun.reactions.util.parameter.Parameterizable;
import org.jetbrains.annotations.NotNull;

public interface Activity extends Named {
    boolean proceed(@NotNull Environment env, @NotNull String paramsStr);

    boolean requiresPlayer();

    // TODO
    default boolean isAsync() {
        return true;
    }

    interface Stored<A extends Activity> extends Parameterizable {
        @NotNull A getActivity();

        @NotNull String getContent();

        boolean hasPlaceholders();
    }
}
