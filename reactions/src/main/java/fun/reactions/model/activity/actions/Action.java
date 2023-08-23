package fun.reactions.model.activity.actions;

import com.google.common.base.Objects;
import fun.reactions.model.activity.Activity;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface Action extends Activity {
    final class Stored extends Activity.Stored<Action> {
        public Stored(@NotNull Action action, @NotNull String content) {
            super(action, content);
        }

        @Override
        public @NotNull Parameters asParameters() {
            return Parameters.fromMap(Map.of(
                    "activity-type", "action",
                    "activity", activity.getName(),
                    "content", content
            ));
        }

        @Override
        public @NotNull String toString() {
            return activity.getName() + "=" + content;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Activity.Stored<?> other)) return false;
            return other.getActivity() instanceof Action otherActivity
                    && otherActivity == activity
                    && other.getContent().equals(content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(activity.getName(), content);
        }
    }
}
