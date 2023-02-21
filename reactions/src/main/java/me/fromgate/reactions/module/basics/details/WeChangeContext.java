
package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.WEChangeActivator;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.property;
import static me.fromgate.reactions.logic.context.Variable.simple;

/**
 * Created by MaxDikiy on 17/10/2017.
 */
public class WeChangeContext extends ActivationContext {

    private final Location location;
    private final Material blockType;

    public WeChangeContext(Player player, Location location, Material blockType) {
        super(player);
        this.location = location;
        this.blockType = blockType;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return WEChangeActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, property(false),
                "blocktype", simple(blockType),
                "blocklocation", simple(LocationUtils.locationToString(location))
        );
    }

    public Location getLocation() {
        return this.location;
    }

    public Material getBlockType() {
        return this.blockType;
    }
}
