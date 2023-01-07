package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.GodStorage;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-10-28.
 */
public class GodActivator extends Activator {
    private final GodType god;

    private GodActivator(ActivatorLogic base, GodType type) {
        super(base);
        this.god = type;
    }

    public static GodActivator create(ActivatorLogic base, Parameters param) {
        GodType type = GodType.getByName(param.getString("god", "ANY"));
        return new GodActivator(base, type);
    }

    public static GodActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        GodType type = GodType.getByName(cfg.getString("god", "ANY"));
        return new GodActivator(base, type);
    }

    @Override
    public boolean checkStorage(@NotNull Storage event) {
        GodStorage e = (GodStorage) event;
        return checkGod(e.isGod());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("god", god.name());
    }

    private boolean checkGod(boolean isGod) {
        return switch (god) {
            case ANY -> true;
            case TRUE -> isGod;
            case FALSE -> !isGod;
        };
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "god:" + this.god.name() +
                ")";
        return sb;
    }

    private enum GodType {
        TRUE,
        FALSE,
        ANY;

        public static GodType getByName(String godStr) {
            if (godStr.equalsIgnoreCase("true")) return GodType.TRUE;
            if (godStr.equalsIgnoreCase("any")) return GodType.ANY;
            return GodType.FALSE;
        }
    }
}
