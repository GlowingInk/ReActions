package fun.reactions.logic.activators;

import fun.reactions.logic.Logic;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class Activator {
    protected final Logic logic;

    public Activator(@NotNull Logic logic) {
        this.logic = logic;
    }

    /**
     * Execution of activator
     *
     * @param context Details with data for activator
     */
    public final void executeActivator(@NotNull ActivationContext context) {
        if (!checkContext(context)) return;
        logic.execute(context.createEnvironment(logic.getPlatform(), logic.getName()));
    }

    /**
     * Get activator logic
     *
     * @return Related activator logic
     */
    public final @NotNull Logic getLogic() {
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
     * @param context Details with data for trigger
     * @return Are checks successfully past
     */
    protected abstract boolean checkContext(@NotNull ActivationContext context);

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
