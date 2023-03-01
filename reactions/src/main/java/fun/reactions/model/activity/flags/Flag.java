package fun.reactions.model.activity.flags;

import com.google.common.base.Objects;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public interface Flag extends Activity {
    /**
     * Check the flag against current context
     * @param env activation context
     * @param paramsStr parameters of flag
     * @return is flag satisfied
     */
    @Override
    boolean proceed(@NotNull Environment env, @NotNull String paramsStr);

    final class Stored implements Activity.Stored<Flag> {
        private final Flag flag;
        private final String content;
        private final boolean inverted;
        private final boolean placeholders;

        public Stored(@NotNull Flag flag, @NotNull String content, boolean inverted) {
            this.flag = flag;
            this.content = content;
            this.inverted = inverted;
            this.placeholders = content.indexOf('%') != -1;
        }

        @Override
        public @NotNull Flag getActivity() {
            return flag;
        }

        @Override
        public @NotNull String getContent() {
            return content;
        }

        public boolean isInverted() {
            return inverted;
        }

        @Override
        public boolean hasPlaceholders() {
            return placeholders;
        }

        @Override
        public @NotNull String toString() {
            return (inverted ? "!" : "") + flag.getName() + "=" + content;
        }

        @Override
        public @NotNull Parameters asParameters() {
            return Parameters.singleton(flag.getName(), content);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Flag.Stored other && other.flag == flag && other.inverted == inverted && other.content.equals(content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(flag.getName(), content, inverted);
        }
    }
}
