package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.module.basics.context.DamageContext;
import fun.reactions.util.enums.DamageType;
import fun.reactions.util.parameter.Parameters;
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

    private DamageActivator(Logic base, String cause, DamageType source) {
        super(base);
        this.damageCause = cause;
        this.source = source;
    }

    public static DamageActivator create(Logic base, Parameters param) {
        String cause = param.getString("cause", "ANY");
        DamageType source = DamageType.getByName(param.getString("source", "ANY"));
        return new DamageActivator(base, cause, source);
    }

    public static DamageActivator load(Logic base, ConfigurationSection cfg) {
        String cause = cfg.getString("cause", "ANY");
        DamageType source = DamageType.getByName(cfg.getString("source", "ANY"));
        return new DamageActivator(base, cause, source);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        DamageContext de = (DamageContext) context;
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
