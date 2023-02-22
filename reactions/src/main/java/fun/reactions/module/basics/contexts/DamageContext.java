package fun.reactions.module.basics.contexts;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.DamageActivator;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-07-23.
 */
public class DamageContext extends ActivationContext {
    public static final String DAMAGE = "damage";

    private final DamageCause cause;
    private final String source; // TODO enum
    private final double damage;
    private final double finalDamage;

    public DamageContext(@NotNull Player player, @NotNull DamageCause cause, @NotNull String source, double damage, double finalDamage) {
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

    public @NotNull DamageCause getCause() {
        return this.cause;
    }

    public @NotNull String getSource() {
        return this.source;
    }
}
