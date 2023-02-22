
package fun.reactions.module.worldedit.contexts;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.worldedit.activators.WeChangeActivator;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

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
        return WeChangeActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, Variable.property(false),
                "blocktype", Variable.simple(blockType),
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
