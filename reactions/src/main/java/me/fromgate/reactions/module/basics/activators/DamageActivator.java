package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.details.DamageDetails;
import me.fromgate.reactions.util.enums.DamageType;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-07-23.
 */
// TODO: Assemble to one activator
public class DamageActivator extends Activator {
    private final String damageCause;
    private final DamageType source;

    private DamageActivator(ActivatorLogic base, String cause, DamageType source) {
        super(base);
        this.damageCause = cause;
        this.source = source;
    }

    public static DamageActivator create(ActivatorLogic base, Parameters param) {
        String cause = param.getString("cause", "ANY");
        DamageType source = DamageType.getByName(param.getString("source", "ANY"));
        return new DamageActivator(base, cause, source);
    }

    public static DamageActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String cause = cfg.getString("cause", "ANY");
        DamageType source = DamageType.getByName(cfg.getString("source", "ANY"));
        return new DamageActivator(base, cause, source);
    }

    @Override
    public boolean checkDetails(@NotNull Details event) {
        DamageDetails de = (DamageDetails) event;
        if (!damageCauseCheck(de.getCause())) return false;
        return sourceCheck(de.getSource());
    }

    private boolean damageCauseCheck(EntityDamageEvent.DamageCause dc) {
        if (damageCause.equals("ANY")) return true;
        return dc.name().equals(damageCause);
    }

    private boolean sourceCheck(String st) {
        if (source.name().equals("ANY")) return true;
        return st.equals(source.name());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("cause", this.damageCause);
        cfg.set("source", this.source.name());
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "cause:" + this.damageCause +
                "; source:" + this.source.name() +
                ")";
        return sb;
    }
}
