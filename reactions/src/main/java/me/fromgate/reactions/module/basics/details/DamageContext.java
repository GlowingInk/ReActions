package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.environment.Variable;
import me.fromgate.reactions.module.basics.activators.DamageActivator;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static me.fromgate.reactions.logic.environment.Variable.property;
import static me.fromgate.reactions.logic.environment.Variable.simple;

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
        vars.put(CANCEL_EVENT, property(false));
        vars.put(DAMAGE, property(damage));
        vars.put("final_damage", simple(finalDamage));
        vars.put("cause", simple(cause));
        vars.put("source", simple(source));
        return vars;
    }

    public @NotNull DamageCause getCause() {
        return this.cause;
    }

    public @NotNull String getSource() {
        return this.source;
    }
}
