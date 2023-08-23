package fun.reactions.model.activity;

import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Named;
import fun.reactions.util.parameter.Parameterizable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Activity extends Named {
    boolean proceed(@NotNull Environment env, @NotNull String paramsStr);

    // TODO
    default boolean isAsync() {
        return true;
    }

    interface Personal extends Activity {
        @Override
        default boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
            Player player = env.getPlayer();
            return player != null && proceed(env, player, paramsStr);
        }

        boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr);
    }

    abstract class Stored<A extends Activity> implements Parameterizable {
        protected final A activity;
        protected final String content;
        protected final boolean placeholders;

        public Stored(@NotNull A activity, @NotNull String content) {
            this.activity = activity;
            this.content = content;
            this.placeholders = content.indexOf('%') != -1;
        }

        public @NotNull A getActivity() {
            return activity;
        }

        public @NotNull String getContent() {
            return content;
        }

        public boolean hasPlaceholders() {
            return placeholders;
        }
    }
}
