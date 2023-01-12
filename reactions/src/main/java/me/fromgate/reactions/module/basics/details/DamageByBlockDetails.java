package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.data.DoubleValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.activators.DamageByBlockActivator;
import me.fromgate.reactions.util.collections.Maps;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-07-23.
 */
public class DamageByBlockDetails extends Details {

    private final Block blockDamager;
    private final DamageCause cause;
    private final double damage;


    public DamageByBlockDetails(Player player, Block blockDamager, double damage, DamageCause cause) {
        super(player);
        this.blockDamager = blockDamager;
        this.damage = damage;
        this.cause = cause;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return DamageByBlockActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("blocklocation", LocationUtils.locationToString(blockDamager.getLocation()));
        tempVars.put("blocktype", blockDamager.getType().name());
        tempVars.put("block", blockDamager.getType().name());
        tempVars.put("cause", cause.name());
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return new Maps.Builder<String, DataValue>()
                .put(CANCEL_EVENT, new BooleanValue(false))
                .put(DamageDetails.DAMAGE, new DoubleValue(damage))
                .build();
    }

    public Block getBlockDamager() {return this.blockDamager;}

    public DamageCause getCause() {return this.cause;}

    public double getDamage() {return this.damage;}
}
