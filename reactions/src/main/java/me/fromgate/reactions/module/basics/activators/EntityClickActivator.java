package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.EntityClickStorage;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 * Created by MaxDikiy on 2017-05-14.
 */
public class EntityClickActivator extends Activator {
    private final EntityType entityType;

    private EntityClickActivator(ActivatorLogic base, String entityType) {
        super(base);
        this.entityType = Utils.getEnum(EntityType.class, entityType);
    }

    public static EntityClickActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String entityType = cfg.getString("entity-type");
        return new EntityClickActivator(base, entityType);
    }

    public static EntityClickActivator create(ActivatorLogic base, Parameters param) {
        String entityType = param.getString("type", "");
        return new EntityClickActivator(base, entityType);
    }

    @Override
    public boolean checkStorage(Storage event) {
        EntityClickStorage ece = (EntityClickStorage) event;
        if (ece.getEntity() == null) return false;
        return isActivatorEntity(ece.getEntity());
    }

    private boolean isActivatorEntity(Entity entity) {
        return this.entityType == null || entity.getType() == entityType;
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("entity-type", entityType.name());
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "type:" + (entityType == null ? "-" : entityType.name()) +
                ")";
        return sb;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
