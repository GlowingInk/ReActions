package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.TeleportActivator;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.property;

public class TeleportDetails extends Details {
    public static final String LOCATION_TO = "loc_to";

    private final TeleportCause cause;
    private final String worldTo;
    private final Location to;

    public TeleportDetails(Player player, TeleportCause cause, Location to) {
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
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, property(false),
                LOCATION_TO, property(LocationUtils.locationToString(to))
        );
    }

    public TeleportCause getCause() {
        return this.cause;
    }

    public String getWorldTo() {
        return this.worldTo;
    }
}
