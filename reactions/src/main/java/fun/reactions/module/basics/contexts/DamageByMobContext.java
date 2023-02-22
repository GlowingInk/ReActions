package fun.reactions.module.basics.contexts;

import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.DamageByMobActivator;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.mob.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by MaxDikiy on 2017-06-25.
 */
public class DamageByMobContext extends DamageContext {

    private final Entity damager;

    public DamageByMobContext(Player player, Entity damager, DamageCause cause, double damage, double finalDamage) {
        super(player, cause, "ENTITY", damage, finalDamage);
        this.damager = damager;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return DamageByMobActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        Map<String, Variable> vars =  super.prepareVariables();
        vars.put("damagerlocation", Variable.simple(LocationUtils.locationToString(damager.getLocation())));
        vars.put("damagertype", Variable.simple(damager.getType()));
        vars.put("entitytype", Variable.simple(damager.getType())); // FIXME Why there is a copy?
        vars.put("damagername", Variable.simple(EntityUtils.getEntityDisplayName(damager)));
        return vars;
    }

    public @NotNull Entity getDamager() {
        return this.damager;
    }
}
