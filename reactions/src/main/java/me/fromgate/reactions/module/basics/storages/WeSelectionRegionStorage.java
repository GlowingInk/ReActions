package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.externals.worldedit.WeSelection;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.WESelectionActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.BooleanValue;
import me.fromgate.reactions.util.data.DataValue;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WeSelectionRegionStorage extends Storage {
    private final WeSelection selection;

    public WeSelectionRegionStorage(Player player, WeSelection weSelection) {
        super(player);
        this.selection = weSelection;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return WESelectionActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        if (selection.isValid()) {
            tempVars.put("seltype", selection.selType());
            World world = selection.world();
            tempVars.put("world", (world != null) ? world.getName() : "");
            tempVars.put("selblocks", Integer.toString(selection.area()));
            tempVars.put("region", selection.region());
        }
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return MapBuilder.single(CANCEL_EVENT, new BooleanValue(false));
    }

    public WeSelection getSelection() {return this.selection;}
}
