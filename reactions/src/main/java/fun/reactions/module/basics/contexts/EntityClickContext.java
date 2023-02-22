package fun.reactions.module.basics.contexts;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.EntityClickActivator;
import fun.reactions.util.mob.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

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
                CANCEL_EVENT, Variable.property(false),
                "entitytype", Variable.simple(entity.getType()),
                "entity_name", simple(EntityUtils.getEntityDisplayName(entity))
        );
    }

    public Entity getEntity() {
        return this.entity;
    }
}
