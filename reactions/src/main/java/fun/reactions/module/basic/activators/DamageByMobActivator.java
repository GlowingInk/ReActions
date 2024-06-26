package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
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

    private static String getCauseByName(String damageCauseStr) {
        if (damageCauseStr != null) {
            for (EntityDamageEvent.DamageCause damageCause : EntityDamageEvent.DamageCause.values()) {
                if (damageCauseStr.equalsIgnoreCase(damageCause.name())) {
                    return damageCause.name();
                }
            }
        }
        return "ANY";
    }

    private static String getEntityTypeByName(String sType) {
        if (sType != null) {
            sType = sType.toUpperCase(Locale.ROOT);
            for (EntityType type : EntityType.values()) {
                if (sType.equals(type.name()))
                    return type.name();
            }
        }
        return "ANY";
    }

    public static DamageByMobActivator create(Logic base, Parameters param) {
        String damagerType = param.originValue();
        String damagerName;
        if (damagerType.contains("$")) {
            damagerName = getEntityTypeByName(damagerType.substring(0, damagerType.indexOf('$')));
            damagerType = damagerType.substring(damagerName.length() + 1);
        } else {
            damagerType = getEntityTypeByName(param.getString("type", "ANY"));
            damagerName = param.getString("name");
        }
        damagerName = ChatColor.translateAlternateColorCodes('&', damagerName.replace("\\_", " "));
        String entityType = getEntityTypeByName(param.getString("etype", "ANY"));
        String damageCause = getCauseByName(param.getString("cause", "ANY"));
        return new DamageByMobActivator(base, damagerType, damagerName, entityType, damageCause);
    }

    public static DamageByMobActivator load(Logic base, ConfigurationSection cfg) {
        String damagerName = cfg.getString("damager-name", "");
        String damagerType = cfg.getString("damager-type", "");
        String entityType = cfg.getString("entity-type", "");
        String cause = cfg.getString("cause", "");
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

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "type:" + (damagerType.isEmpty() ? "-" : damagerType.toUpperCase(Locale.ROOT)) +
                "; name:" + (damagerName.isEmpty() ? "-" : damagerName) +
                "; etype:" + (entityType.isEmpty() ? "-" : entityType.toUpperCase(Locale.ROOT)) +
                "; cause:" + damageCause +
                ")";
        return sb;
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
            vars.put("damagerlocation", Variable.simple(LocationUtils.locationToString(damager.getLocation())));
            vars.put("damagertype", Variable.simple(damager.getType()));
            vars.put("entitytype", Variable.simple(damager.getType())); // FIXME Why there is a copy?
            vars.put("damagername", Variable.simple(EntityUtils.getEntityDisplayName(damager)));
            return vars;
        }
    }
}
