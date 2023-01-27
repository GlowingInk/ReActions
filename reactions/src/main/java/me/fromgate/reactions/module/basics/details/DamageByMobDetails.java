package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.DamageByMobActivator;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.mob.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.plain;

/**
 * Created by MaxDikiy on 2017-06-25.
 */
public class DamageByMobDetails extends DamageDetails {

    private final Entity damager;

    public DamageByMobDetails(Player player, Entity damager, DamageCause cause, double damage, double finalDamage) {
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
        vars.put("damagerlocation", plain(LocationUtils.locationToString(damager.getLocation())));
        vars.put("damagertype", plain(damager.getType()));
        vars.put("entitytype", plain(damager.getType())); // FIXME Why there is a copy?
        vars.put("damagername", plain(EntityUtils.getEntityDisplayName(damager)));
        return vars;
    }

    public @NotNull Entity getDamager() {
        return this.damager;
    }
}
