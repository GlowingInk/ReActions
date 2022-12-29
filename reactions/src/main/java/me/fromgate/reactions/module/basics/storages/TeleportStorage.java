package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.data.LocationValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.TeleportActivator;
import me.fromgate.reactions.util.collections.Maps;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TeleportStorage extends Storage {
    public static final String LOCATION_TO = "loc_to";

    private final TeleportCause cause;
    private final String worldTo;
    private final Location to;

    public TeleportStorage(Player player, TeleportCause cause, Location to) {
        super(player);
        this.cause = cause;
        this.worldTo = to.getWorld().getName();
        this.to = to;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return TeleportActivator.class;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return new Maps.Builder<String, DataValue>()
                .put(CANCEL_EVENT, new BooleanValue(false))
                .put(LOCATION_TO, new LocationValue(to))
                .build();
    }

    public TeleportCause getCause() {return this.cause;}

    public String getWorldTo() {return this.worldTo;}

    public Location getTo() {return this.to;}
}
