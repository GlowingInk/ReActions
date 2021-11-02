package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.*;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.BooleanValue;
import me.fromgate.reactions.util.data.DataValue;
import me.fromgate.reactions.util.data.LocationValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

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
    public Class<? extends Activator> getType() {
        return TeleportActivator.class;
    }

    @Override
    protected Map<String, DataValue> prepareChangeables() {
        return new MapBuilder<String, DataValue>()
                .put(CANCEL_EVENT, new BooleanValue(false))
                .put(LOCATION_TO, new LocationValue(to))
                .build();
    }

    public TeleportCause getCause() {return this.cause;}

    public String getWorldTo() {return this.worldTo;}

    public Location getTo() {return this.to;}
}
