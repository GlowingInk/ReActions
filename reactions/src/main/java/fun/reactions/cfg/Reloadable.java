package fun.reactions.cfg;

import org.jetbrains.annotations.NotNull;

public interface Reloadable {
    void acceptReload(@NotNull RaConfiguration config);
}
