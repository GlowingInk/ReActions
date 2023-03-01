package fun.reactions.model.activity.actions;

import com.google.common.base.Objects;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public interface Action extends Activity {
    /**
     * Execute an action
     * @param env context of activation
     * @param paramsStr parameters of action
     * @return is action executed successfully
     */
    @Override
    boolean proceed(@NotNull Environment env, @NotNull String paramsStr);

    final class Stored implements Activity.Stored<Action> {
        private final Action action;
        private final String content;
        private final boolean placeholders;

        public Stored(@NotNull Action action, @NotNull String content) {
            this.action = action;
            this.content = content;
            this.placeholders = content.indexOf('%') != -1;
        }

        @Override
        public @NotNull Action getActivity() {
            return action;
        }

        @Override
        public @NotNull String getContent() {
            return content;
        }

        @Override
        public boolean hasPlaceholders() {
            return placeholders;
        }

        @Override
        public @NotNull Parameters asParameters() {
            return Parameters.singleton(action.getName(), content);
        }

        @Override
        public @NotNull String toString() {
            return action.getName() + "=" + content;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Action.Stored other && other.action == action && other.content.equals(content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(action.getName(), content);
        }
    }
}
