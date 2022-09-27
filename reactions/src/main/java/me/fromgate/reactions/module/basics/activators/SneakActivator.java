package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.SneakStorage;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by MaxDikiy on 2017-05-16.
 */
public class SneakActivator extends Activator {
    private final SneakType sneak;

    private SneakActivator(ActivatorLogic base, SneakType sneak) {
        super(base);
        this.sneak = sneak;
    }

    public static SneakActivator create(ActivatorLogic base, Parameters param) {
        SneakType sneak = SneakType.getByName(param.getString("sneak", "ANY"));
        return new SneakActivator(base, sneak);
    }

    public static SneakActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        SneakType sneak = SneakType.getByName(cfg.getString("sneak", "ANY"));
        return new SneakActivator(base, sneak);
    }

    @Override
    public boolean checkStorage(Storage event) {
        SneakStorage se = (SneakStorage) event;
        return checkSneak(se.isSneaking());
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("sneak", sneak.name());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    private boolean checkSneak(boolean isSneak) {
        return switch (sneak) {
            case ANY -> true;
            case TRUE -> isSneak;
            case FALSE -> !isSneak;
        };
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "sneak:" + this.sneak.name() +
                ")";
        return sb;
    }

    enum SneakType {
        TRUE,
        FALSE,
        ANY;

        public static SneakType getByName(String sneakStr) {
            if (sneakStr.equalsIgnoreCase("true")) return SneakType.TRUE;
            if (sneakStr.equalsIgnoreCase("any")) return SneakType.ANY;
            return SneakType.FALSE;
        }
    }
}
