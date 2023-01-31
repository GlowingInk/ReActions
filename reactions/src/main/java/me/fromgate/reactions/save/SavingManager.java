package me.fromgate.reactions.save;

import me.fromgate.reactions.ReActions;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class SavingManager implements Listener {
    private final Set<Saveable> saveables;
    private final Server server;

    public SavingManager(@NotNull ReActions.Platform rea) {
        this.server = rea.getServer();
        this.saveables = new HashSet<>();

        this.server.getPluginManager().registerEvents(this, rea.getPlugin());
    }

    public void register(@NotNull Saveable saveable) {
        this.saveables.add(saveable);
    }

    @EventHandler
    public void onSave(WorldSaveEvent event) {
        if (event.getWorld() == server.getWorlds().get(0)) {
            saveables.forEach(Saveable::save);
        }
    }
}
