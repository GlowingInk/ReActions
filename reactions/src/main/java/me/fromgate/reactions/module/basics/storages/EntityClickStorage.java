package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.EntityClickActivator;
import me.fromgate.reactions.util.collections.Maps;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-05-14.
 */
public class EntityClickStorage extends Storage {

    private final Entity entity;

    public EntityClickStorage(Player p, Entity entity) {
        super(p);
        this.entity = entity;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return EntityClickActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("entitytype", entity.getType().name());
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return Maps.Builder.single(CANCEL_EVENT, new BooleanValue(false));
    }

    public Entity getEntity() {return this.entity;}
}
