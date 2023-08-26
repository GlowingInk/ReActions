package fun.reactions.module.basic.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.Utils;
import fun.reactions.util.naming.Aliased;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Aliased.Names("IS_ONLINE")
public class CheckOnlineFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        UUID id = Utils.parseUniqueId(paramsStr);
        return (id == null ? Bukkit.getPlayerExact(paramsStr) : Bukkit.getPlayer(id)) != null;
    }

    @Override
    public @NotNull String getName() {
        return "CHECK_ONLINE";
    }

}
