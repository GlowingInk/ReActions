package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.Utils;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

/**
 * @author MaxDikiy
 * @since 14/05/2017
 */
public class EntityClickActivator extends Activator {
    private final EntityType entityType;

    private EntityClickActivator(Logic base, String entityType) {
        super(base);
        this.entityType = Utils.getEnum(EntityType.class, entityType);
    }

    public static EntityClickActivator load(Logic base, ConfigurationSection cfg) {
        String entityType = cfg.getString("entity-type");
        return new EntityClickActivator(base, entityType);
    }

    public static EntityClickActivator create(Logic base, Parameters param) {
        String entityType = param.getString("type", "");
        return new EntityClickActivator(base, entityType);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context ece = (Context) context;
        if (ece.entity == null) return false;
        return isActivatorEntity(ece.entity);
    }

    private boolean isActivatorEntity(Entity entity) {
        return this.entityType == null || entity.getType() == entityType;
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("entity-type", entityType == null ? null : entityType.name());
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "type:" + (entityType == null ? "-" : entityType.name()) +
                ")";
        return sb;
    }

    /**
     * @author MaxDikiy
     * @since 14/05/2017
     */
    public static class Context extends ActivationContext {

        private final Entity entity;

        public Context(Player p, Entity entity) {
            super(p);
            this.entity = entity;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return EntityClickActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(
                    CANCEL_EVENT, Variable.property(false),
                    "entitytype", Variable.simple(entity.getType()),
                    "entity_name", simple(EntityUtils.getEntityDisplayName(entity))
            );
        }
    }
}
