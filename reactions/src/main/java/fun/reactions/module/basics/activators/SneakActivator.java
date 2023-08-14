package fun.reactions.module.basics.activators;

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
 * Created by MaxDikiy on 2017-05-16.
 */
public class SneakActivator extends Activator {
    private final TriBoolean sneak;

    private SneakActivator(Logic base, TriBoolean sneak) {
        super(base);
        this.sneak = sneak;
    }

    public static SneakActivator create(Logic base, Parameters param) {
        return new SneakActivator(base, param.getTriBoolean("sneak"));
    }

    public static SneakActivator load(Logic base, ConfigurationSection cfg) {
        return new SneakActivator(base, TriBoolean.byString(cfg.getString("sneak")));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context se = (Context) context;
        return sneak.isValidFor(se.sneaking);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("sneak", sneak.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                "sneak:" + this.sneak.name() +
                ")";
    }

    /**
     * Created by MaxDikiy on 2017-05-16.
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
            return Map.of("sneak", Variable.simple(sneaking));
        }
    }
}
