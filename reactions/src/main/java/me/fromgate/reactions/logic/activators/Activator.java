package me.fromgate.reactions.logic.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import org.bukkit.configuration.ConfigurationSection;

public abstract class Activator {
    protected final ActivatorLogic logic;

    public Activator(ActivatorLogic logic) {
        this.logic = logic;
    }

    /**
     * Execution of activator
     *
     * @param storage Storage with data for activator
     */
    public final void executeActivator(Storage storage) {
        if (!checkStorage(storage)) return;
        logic.executeLogic(storage.generateContext(logic.getName()));
    }

    /**
     * Get activator logic
     *
     * @return Related activator logic
     */
    public final ActivatorLogic getLogic() {
        return logic;
    }

    /**
     * Save activator to config with actions, reactions and flags
     *
     * @param cfg Section of activator
     */
    public final void saveActivator(ConfigurationSection cfg) {
        saveOptions(cfg);
        logic.save(cfg);
    }

    /**
     * Check trigger options
     *
     * @param storage Storage with data for trigger
     * @return Are checks successfully past
     */
    protected abstract boolean checkStorage(Storage storage);

    /**
     * Save activator options to the config
     *
     * @param cfg Section of activator
     */
    public void saveOptions(ConfigurationSection cfg) {
        // Sometimes we don't need that
    }

    /**
     * TODO: Actually pretty useless right now
     * Check if trigger is valid
     *
     * @return Is trigger valid
     */
    public boolean isValid() {
        return true;
    }

    @Override
    public int hashCode() {
        return logic.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(logic.getGroup()).append(", ").append(logic.getName()).append(" [").append(getClass().getSimpleName()).append("]");
        sb.append(logic);
        return sb.toString();
    }

}
