package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.environment.Variable;
import me.fromgate.reactions.module.basics.activators.EntityClickActivator;
import me.fromgate.reactions.util.mob.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.environment.Variable.property;
import static me.fromgate.reactions.logic.environment.Variable.simple;

/**
 * Created by MaxDikiy on 2017-05-14.
 */
public class EntityClickContext extends ActivationContext {

    private final Entity entity;

    public EntityClickContext(Player p, Entity entity) {
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
                CANCEL_EVENT, property(false),
                "entitytype", simple(entity.getType()),
                "entity_name", simple(EntityUtils.getEntityDisplayName(entity))
        );
    }

    public Entity getEntity() {
        return this.entity;
    }
}
