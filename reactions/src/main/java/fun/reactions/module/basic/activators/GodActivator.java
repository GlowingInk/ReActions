package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.enums.TriBoolean;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author MaxDikiy
 * @since 28/10/2017
 */
public class GodActivator extends Activator {
    private final TriBoolean god;

    private GodActivator(Logic base, TriBoolean type) {
        super(base);
        this.god = type;
    }

    public static GodActivator create(Logic base, Parameters param) {
        return new GodActivator(base, param.getTriBoolean("god"));
    }

    public static GodActivator load(Logic base, ConfigurationSection cfg) {
        return new GodActivator(base, TriBoolean.byString(cfg.getString("god")));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context e = (Context) context;
        return god.isValidFor(e.god);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("god", god.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                "god:" + this.god.name() +
                ")";
    }

    /**
     * @author MaxDikiy
     * @since 27/10/2017
     */
    public static class Context extends ActivationContext {
        private final boolean god;

        public Context(Player player, boolean god) {
            super(player);
            this.god = god;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return GodActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(
                    CANCEL_EVENT, Variable.property(false),
                    "god", Variable.simple(god)
            );
        }
    }
}
