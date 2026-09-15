package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.variables.EntityVariable;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author MaxDikiy
 * @since 25/06/2017
 */
// TODO: Assemble to one activator
public class DamageByMobActivator extends Activator {

    private final String damagerName;
    // TODO: Use EntityType
    private final String damagerType;
    // TODO: Use EntityType
    private final String entityType;
    // TODO: Use Enum
    private final String damageCause;

    private DamageByMobActivator(Logic base, String damagerName, String damagerType, String entityType, String damageCause) {
        super(base);
        this.damagerName = damagerName;
        this.damagerType = damagerType;
        this.entityType = entityType;
        this.damageCause = damageCause;
    }

    public static DamageByMobActivator create(Logic base, Parameters param) {
        String damagerName = param.getString("damager-name", "");
        String damagerType = param.getString("damager-type", "");
        String entityType = param.getString("entity-type", "");
        String cause = param.getString("cause", "");
        return new DamageByMobActivator(base, damagerName, damagerType, entityType, cause);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context pde = (Context) context;
        if (damagerType.isEmpty()) return false;
        Entity damager = pde.damager;
        if (damager != null && !isActivatorDamager(damager)) return false;
        return damageCauseCheck(pde.cause);
    }

    private boolean isActivatorDamager(Entity damager) {
        if (!damagerName.isEmpty() && damagerName.equals(getMobName(damager))) return false;
        if (damagerType.equalsIgnoreCase("ANY")) return true;
        return damager.getType().name().equalsIgnoreCase(this.damagerType);
    }

    private String getMobName(Entity mob) {
        return mob.getCustomName() == null ? "" : mob.getCustomName();
    }

    private boolean damageCauseCheck(EntityDamageEvent.DamageCause dc) {
        if (damageCause.equals("ANY")) return true;
        return dc.name().equals(damageCause);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("damager-type", damagerType);
        cfg.set("damager-name", damagerName);
        cfg.set("entity-type", entityType);
        cfg.set("cause", damageCause);
    }

    /**
     * @author MaxDikiy
     * @since 25/06/2017
     */
    public static class Context extends DamageActivator.Context {

        private final Entity damager;

        public Context(Player player, Entity damager, EntityDamageEvent.DamageCause cause, double damage, double finalDamage) {
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
            vars.put("damagerlocation", Variable.value(LocationUtils.locationToString(damager.getLocation())));
            vars.put("damagertype", Variable.value(damager.getType()));
            vars.put("entitytype", Variable.value(damager.getType()));
            vars.put("damagername", Variable.value(EntityUtils.getEntityDisplayName(damager)));
            vars.put("entity", new EntityVariable(damager));
            return vars;
        }
    }
}
