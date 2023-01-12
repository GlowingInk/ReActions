package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.activators.SneakActivator;
import me.fromgate.reactions.util.collections.Maps;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by MaxDikiy on 2017-05-16.
 */
public class SneakDetails extends Details {

    private final boolean sneaking;

    public SneakDetails(Player player, boolean sneaking) {
        super(player);
        this.sneaking = sneaking;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return SneakActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        return Maps.Builder.single("sneak", Boolean.toString(sneaking));
    }

    public boolean isSneaking() {return this.sneaking;}
}