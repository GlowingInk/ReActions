package fun.reactions.module.basics.context;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.TeleportActivator;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.property;

public class TeleportContext extends ActivationContext {
    public static final String LOCATION_TO = "loc_to";

    private final TeleportCause cause;
    private final String worldTo;
    private final Location to;

    public TeleportContext(Player player, TeleportCause cause, Location to) {
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
                CANCEL_EVENT, Variable.property(false),
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
