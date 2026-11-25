package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.parameter.Parameters;
import ink.glowing.utils.bool.TriState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author MaxDikiy
 * @since 16/05/2017
 */
public class SneakActivator extends Activator {
    private final TriState sneak;

    private SneakActivator(Logic base, TriState sneak) {
        super(base);
        this.sneak = sneak;
    }

    public static SneakActivator create(Logic base, Parameters param) {
        return new SneakActivator(base, param.getTriState("sneak"));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context se = (Context) context;
        return sneak.isValidFor(se.sneaking);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("sneak", Parameters.TRI.name(sneak));
    }

    /**
     * @author MaxDikiy
     * @since 16/05/2017
     */
    public static class Context extends ActivationContext {
        private final boolean sneaking;

        public Context(Player player, boolean sneaking) {
            super(player);
            this.sneaking = sneaking;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return SneakActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of("sneak", Variable.value(sneaking));
        }
    }
}
