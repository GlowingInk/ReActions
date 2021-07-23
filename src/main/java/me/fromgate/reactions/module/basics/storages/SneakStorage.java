package me.fromgate.reactions.module.basics.storages;

import lombok.Getter;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.SneakActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Created by MaxDikiy on 2017-05-16.
 */
@Getter
public class SneakStorage extends Storage {

    private final boolean sneaking;

    public SneakStorage(Player player, boolean sneaking) {
        super(player);
        this.sneaking = sneaking;
    }

    @Override
    public Class<? extends Activator> getType() {
        return SneakActivator.class;
    }

    @Override
    protected Map<String, String> prepareVariables() {
        return MapBuilder.single("sneak", Boolean.toString(sneaking));
    }
}