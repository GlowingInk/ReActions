package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.details.GodDetails;
import me.fromgate.reactions.util.enums.TriBoolean;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-10-28.
 */
public class GodActivator extends Activator {
    private final TriBoolean god;

    private GodActivator(ActivatorLogic base, TriBoolean type) {
        super(base);
        this.god = type;
    }

    public static GodActivator create(ActivatorLogic base, Parameters param) {
        return new GodActivator(base, param.getTriBoolean("god"));
    }

    public static GodActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        return new GodActivator(base, TriBoolean.getByName(cfg.getString("god")));
    }

    @Override
    public boolean checkDetails(@NotNull Details event) {
        GodDetails e = (GodDetails) event;
        return god.isValidFor(e.isGod());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("god", god.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                "god:" + this.god.name() +
                ")";
    }
}
