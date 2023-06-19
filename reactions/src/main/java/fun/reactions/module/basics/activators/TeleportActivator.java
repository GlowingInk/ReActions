package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.Utils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.property;

@Aliased.Names("TP")
public class TeleportActivator extends Activator {
    private final TeleportCause cause;
    private final String worldTo;

    private TeleportActivator(Logic base, TeleportCause cause, String worldTo) {
        super(base);
        this.cause = cause;
        this.worldTo = worldTo;
    }

    public static TeleportActivator create(Logic base, Parameters param) {
        TeleportCause cause = Utils.getEnum(TeleportCause.class, param.getString("cause"));
        String worldTo = param.getString("world");
        return new TeleportActivator(base, cause, worldTo);
    }

    public static TeleportActivator load(Logic base, ConfigurationSection cfg) {
        TeleportCause cause = Utils.getEnum(TeleportCause.class, cfg.getString("cause", ""));
        String worldTo = cfg.getString("world");
        return new TeleportActivator(base, cause, worldTo);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context tpStorage = (Context) context;
        if (cause != null && tpStorage.cause != cause) return false;
        return worldTo == null || tpStorage.worldTo.equalsIgnoreCase(worldTo);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("cause", cause == null ? null : cause.name());
        cfg.set("world", worldTo);
    }

    public static class Context extends ActivationContext {
        public static final String LOCATION_TO = "loc_to";

        private final TeleportCause cause;
        private final String worldTo;
        private final Location to;

        public Context(Player player, TeleportCause cause, Location to) {
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
    }
}
