package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.DamageActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.BooleanValue;
import me.fromgate.reactions.util.data.DataValue;
import me.fromgate.reactions.util.data.DoubleValue;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-07-23.
 */
public class DamageStorage extends Storage {
    public static final String DAMAGE = "damage";

    private final DamageCause cause;
    private final String source;
    private final double damage;

    public DamageStorage(Player player, double damage, DamageCause cause, String source) {
        super(player);
        this.damage = damage;
        this.cause = cause;
        this.source = source;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return DamageActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("damage", Double.toString(damage));
        tempVars.put("cause", cause.name());
        tempVars.put("source", source);
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return new MapBuilder<String, DataValue>()
                .put(CANCEL_EVENT, new BooleanValue(false))
                .put(DAMAGE, new DoubleValue(damage))
                .build();
    }

    public DamageCause getCause() {return this.cause;}

    public String getSource() {return this.source;}

    public double getDamage() {return this.damage;}
}
