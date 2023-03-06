package fun.reactions.model.activity.flags;

import com.google.common.base.Objects;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface Flag extends Activity {
    /**
     * Check the flag against current context
     * @param env activation context
     * @param paramsStr parameters of flag
     * @return is flag satisfied
     */
    @Override
    boolean proceed(@NotNull Environment env, @NotNull String paramsStr);

    final class Stored extends Activity.Stored<Flag> {
        private final boolean inverted;

        public Stored(@NotNull Flag flag, @NotNull String content, boolean inverted) {
            super(flag, content);
            this.inverted = inverted;
        }

        public boolean isInverted() {
            return inverted;
        }

        @Override
        public @NotNull String toString() {
            return (inverted ? "!" : "") + activity.getName() + "=" + content;
        }

        @Override
        public @NotNull Parameters asParameters() {
            return Parameters.fromMap(Map.of(
                    "activity-type", "flag",
                    "activity", activity.getName(),
                    "content", content,
                    "inverted", Boolean.toString(inverted)
            ));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Activity.Stored<?> other)) return false;
            return other.getActivity() instanceof Flag otherActivity
                    && otherActivity == activity
                    && other.getContent().equals(content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(activity.getName(), content, inverted);
        }
    }
}
