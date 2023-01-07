package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.SneakStorage;
import me.fromgate.reactions.util.enums.TriBoolean;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-05-16.
 */
public class SneakActivator extends Activator {
    private final TriBoolean sneak;

    private SneakActivator(ActivatorLogic base, TriBoolean sneak) {
        super(base);
        this.sneak = sneak;
    }

    public static SneakActivator create(ActivatorLogic base, Parameters param) {
        return new SneakActivator(base, param.getTriBoolean("sneak"));
    }

    public static SneakActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        return new SneakActivator(base, TriBoolean.getByName(cfg.getString("sneak")));
    }

    @Override
    public boolean checkStorage(@NotNull Storage event) {
        SneakStorage se = (SneakStorage) event;
        return sneak.isValidFor(se.isSneaking());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("sneak", sneak.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                "sneak:" + this.sneak.name() +
                ")";
    }
}
