package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.environment.Variable;
import me.fromgate.reactions.module.basics.activators.DamageByBlockActivator;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.environment.Variable.simple;

/**
 * Created by MaxDikiy on 2017-07-23.
 */
public class DamageByBlockContext extends DamageContext {

    private final Block blockDamager;

    public DamageByBlockContext(Player player, Block blockDamager, DamageCause cause, double damage, double finalDamage) {
        super(player, cause, "BLOCK", damage, finalDamage);
        this.blockDamager = blockDamager;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return DamageByBlockActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        Map<String, Variable> vars = super.prepareVariables();
        vars.put("blocklocation", simple(LocationUtils.locationToString(blockDamager.getLocation())));
        vars.put("blocktype", simple(blockDamager.getType()));
        vars.put("block", simple(blockDamager.getType())); // FIXME Why there is a copy?
        return vars;
    }

    public Block getBlockDamager() {
        return this.blockDamager;
    }
}
