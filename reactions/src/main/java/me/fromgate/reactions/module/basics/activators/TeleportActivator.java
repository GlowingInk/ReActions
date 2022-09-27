package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.TeleportStorage;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@Aliases("TP")
public class TeleportActivator extends Activator {
    private final TeleportCause cause;
    private final String worldTo;

    private TeleportActivator(ActivatorLogic base, TeleportCause cause, String worldTo) {
        super(base);
        this.cause = cause;
        this.worldTo = worldTo;
    }

    public static TeleportActivator create(ActivatorLogic base, Parameters param) {
        TeleportCause cause = Utils.getEnum(TeleportCause.class, param.getString("cause"));
        String worldTo = param.getString("world");
        return new TeleportActivator(base, cause, worldTo);
    }

    public static TeleportActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        TeleportCause cause = Utils.getEnum(TeleportCause.class, cfg.getString("cause"));
        String worldTo = cfg.getString("world");
        return new TeleportActivator(base, cause, worldTo);
    }

    @Override
    public boolean checkStorage(Storage storage) {
        TeleportStorage tpStorage = (TeleportStorage) storage;
        if (cause != null && tpStorage.getCause() != cause) return false;
        return worldTo == null || tpStorage.getWorldTo().equalsIgnoreCase(worldTo);
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("cause", cause == null ? null : cause.name());
        cfg.set("world", worldTo);
    }
}
