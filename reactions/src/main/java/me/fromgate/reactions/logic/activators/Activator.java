package me.fromgate.reactions.logic.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class Activator {
    protected final ActivatorLogic logic;

    public Activator(@NotNull ActivatorLogic logic) {
        this.logic = logic;
    }

    /**
     * Execution of activator
     *
     * @param details Details with data for activator
     */
    public final void executeActivator(@NotNull Details details) {
        if (!checkDetails(details)) return;
        logic.executeLogic(details.generateEnvironment(logic.getName()));
    }

    /**
     * Get activator logic
     *
     * @return Related activator logic
     */
    public final @NotNull ActivatorLogic getLogic() {
        return logic;
    }

    /**
     * Save activator to config with actions, reactions and flags
     *
     * @param cfg Section of activator
     */
    public final void saveActivator(@NotNull ConfigurationSection cfg) {
        saveOptions(cfg);
        logic.save(cfg);
    }

    /**
     * Check trigger options
     *
     * @param details Details with data for trigger
     * @return Are checks successfully past
     */
    protected abstract boolean checkDetails(@NotNull Details details);

    /**
     * Save activator options to the config
     *
     * @param cfg Section of activator
     */
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        // Sometimes we don't need that
    }

    /**
     * TODO: Validation on creation instead
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
