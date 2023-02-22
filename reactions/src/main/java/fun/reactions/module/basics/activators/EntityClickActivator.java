package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.module.basics.contexts.EntityClickContext;
import fun.reactions.util.Utils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-05-14.
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
        EntityClickContext ece = (EntityClickContext) context;
        if (ece.getEntity() == null) return false;
        return isActivatorEntity(ece.getEntity());
    }

    private boolean isActivatorEntity(Entity entity) {
        return this.entityType == null || entity.getType() == entityType;
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("entity-type", entityType.name());
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "type:" + (entityType == null ? "-" : entityType.name()) +
                ")";
        return sb;
    }
}
