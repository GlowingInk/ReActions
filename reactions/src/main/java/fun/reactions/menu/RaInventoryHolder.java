package fun.reactions.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RaInventoryHolder implements InventoryHolder {
    // TODO: Store temporary variables maybe?
    private final List<String> activators;
    private Inventory inventory;

    public RaInventoryHolder(List<String> activators) {
        this.activators = activators;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public List<String> getActivators() {return this.activators;}

    public void setInventory(Inventory inventory) {this.inventory = inventory; }
}
