package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.enums.DamageType;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author MaxDikiy
 * @since 23/07/2017
 */
// TODO: Assemble to one activator
public class DamageActivator extends Activator {
    private final String damageCause;
    private final DamageType source;

    private DamageActivator(Logic base, String cause, DamageType source) {
        super(base);
        this.damageCause = cause;
        this.source = source;
    }

    public static DamageActivator create(Logic base, Parameters param) {
        String cause = param.getString("cause", "ANY");
        DamageType source = DamageType.getByName(param.getString("source", "ANY"));
        return new DamageActivator(base, cause, source);
    }

    public static DamageActivator load(Logic base, ConfigurationSection cfg) {
        String cause = cfg.getString("cause", "ANY");
        DamageType source = DamageType.getByName(cfg.getString("source", "ANY"));
        return new DamageActivator(base, cause, source);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context de = (Context) context;
        if (!damageCauseCheck(de.cause)) return false;
        return sourceCheck(de.source);
    }

    private boolean damageCauseCheck(EntityDamageEvent.DamageCause dc) {
        if (damageCause.equals("ANY")) return true;
        return dc.name().equals(damageCause);
    }

    private boolean sourceCheck(String st) {
        if (source.name().equals("ANY")) return true;
        return st.equals(source.name());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("cause", this.damageCause);
        cfg.set("source", this.source.name());
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "cause:" + this.damageCause +
                "; source:" + this.source.name() +
                ")";
        return sb;
    }

    /**
     * @author MaxDikiy
     * @since 23/07/2017
     */
    public static class Context extends ActivationContext {
        public static final String DAMAGE = "damage";

        protected final EntityDamageEvent.DamageCause cause;
        private final String source; // TODO enum
        private final double damage;
        private final double finalDamage;

        public Context(@NotNull Player player, @NotNull EntityDamageEvent.DamageCause cause, @NotNull String source, double damage, double finalDamage) {
            super(player);
            this.cause = cause;
            this.source = source;
            this.damage = damage;
            this.finalDamage = finalDamage;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return DamageActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = new HashMap<>();
            vars.put(CANCEL_EVENT, Variable.property(false));
            vars.put(DAMAGE, Variable.property(damage));
            vars.put("final_damage", Variable.simple(finalDamage));
            vars.put("cause", Variable.simple(cause));
            vars.put("source", Variable.simple(source));
            return vars;
        }
    }
}
