package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;

public class ExecActivator extends Activator {
    private ExecActivator(ActivatorLogic base) {
        super(base);
    }

    public static Activator create(ActivatorLogic base, Parameters param) {
        return new ExecActivator(base);
    }

    public static Activator load(ActivatorLogic base, ConfigurationSection cfg) {
        return new ExecActivator(base);
    }

    @Override
    public boolean checkStorage(Storage storage) {
        return true;
    }

}
