package me.fromgate.reactions.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class TemporaryOp {

    private static final Set<UUID> tempOps = new HashSet<>();

    private TemporaryOp() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static void setOp(CommandSender sender) {
        if (sender instanceof Player && !sender.isOp()) {
            tempOps.add(((Player) sender).getUniqueId());
            sender.setOp(true);
        }
    }

    public static void removeOp(CommandSender sender) {
        if (sender instanceof Player && tempOps.remove(((Player) sender).getUniqueId()))
            sender.setOp(false);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CommandSender> T asOp(T sender) {
        return (T) Proxy.newProxyInstance(
                sender.getClass().getClassLoader(),
                sender.getClass().getInterfaces(),
                (proxy, method, args) -> switch (method.getName()) {
                    case "isOp", "hasPermission" -> true;
                    default -> method.invoke(sender, args);
                }
        );
    }
}
