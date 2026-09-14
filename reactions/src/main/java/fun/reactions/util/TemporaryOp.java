package fun.reactions.util;

import net.kyori.adventure.util.TriState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TemporaryOp {

    private static final Set<UUID> tempOps = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private TemporaryOp() { }

    public static void setOp(CommandSender sender) {
        if (sender instanceof Player player && !player.isOp()) {
            tempOps.add(player.getUniqueId());
            sender.setOp(true);
        }
    }

    public static void removeOp(CommandSender sender) {
        if (sender instanceof Player player && tempOps.remove(player.getUniqueId())) {
            player.setOp(false);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends CommandSender> T asOp(T sender) {
        return (T) Proxy.newProxyInstance(
                sender.getClass().getClassLoader(),
                sender.getClass().getInterfaces(),
                (_, method, args) -> switch (method.getName()) {
                    case "isOp", "hasPermission" -> true;
                    case "permissionValue" -> TriState.TRUE;
                    default -> method.invoke(sender, args);
                }
        );
    }
}
