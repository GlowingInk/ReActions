package fun.reactions.module.basic.activators;

import fun.reactions.ReActions;
import fun.reactions.cfg.RaConfiguration;
import fun.reactions.cfg.Reloadable;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.util.item.VirtualItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class ItemPossessionActivatorBase extends Activator {
    protected final VirtualItem item;

    protected ItemPossessionActivatorBase(@NotNull Logic logic, @NotNull VirtualItem item) {
        super(logic);
        this.item = item;
    }

    public @NotNull VirtualItem getItem() {
        return item;
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("item", item.asString());
    }

    @Override
    public boolean isValid() {
        return item != VirtualItem.ANY && item != VirtualItem.NOTHING;
    }

    public abstract static class Type<A extends ItemPossessionActivatorBase, C extends ActivationContext>
            implements ActivatorType, Reloadable {
        private final ReActions.Platform platform;
        private final Class<A> activatorClass;
        private final String name;
        private final Map<VirtualItem, List<A>> activators = new HashMap<>();
        private final Map<UUID, Possession> possessions = new HashMap<>();
        private int recheck;

        protected Type(@NotNull ReActions.Platform platform, @NotNull Class<A> activatorClass, @NotNull String name) {
            this.platform = platform;
            this.activatorClass = activatorClass;
            this.name = name;
        }

        public static void trigger(
                @NotNull ReActions.Platform platform,
                @NotNull Class<? extends ItemPossessionActivatorBase> activatorClass,
                @NotNull Player player
        ) {
            if (platform.getActivatorTypes().get(activatorClass) instanceof Type<?, ?> type) type.trigger(player);
        }

        protected abstract int recheck(@NotNull RaConfiguration config);

        protected abstract @NotNull List<C> contexts(@NotNull Player player, @NotNull VirtualItem item);

        protected boolean matches(@NotNull A activator, @NotNull C context) {
            return true;
        }

        @Override
        public @NotNull String getName() {
            return name;
        }

        @Override
        public @NotNull Class<A> getActivatorClass() {
            return activatorClass;
        }

        @Override
        public boolean isNeedBlock() {
            return false;
        }

        @Override
        public void include(@NotNull Activator activator) {
            A cast = activatorClass.cast(activator);
            activators.computeIfAbsent(cast.item, _ -> new ArrayList<>()).add(cast);
        }

        @Override
        public void discard(@NotNull Activator activator) {
            A cast = activatorClass.cast(activator);
            List<A> group = activators.get(cast.item);
            if (group != null && group.remove(cast) && group.isEmpty()) activators.remove(cast.item);
        }

        @Override
        public void acceptReload(@NotNull RaConfiguration config) {
            recheck = recheck(config);
            for (var entry : possessions.entrySet()) {
                Possession possession = entry.getValue();
                possession.cancel();
                possession.task = schedule(entry.getKey());
            }
        }

        public void trigger(@NotNull Player player) {
            UUID playerId = player.getUniqueId();
            platform.getServer().getScheduler().runTask(platform.getPlugin(), () -> update(playerId, false));
        }

        private void update(@NotNull UUID playerId, boolean repeat) {
            Possession possession = possessions.get(playerId);
            Set<VirtualItem> previous = possession == null ? Set.of() : possession.items;
            Set<VirtualItem> current = new HashSet<>();
            Player player = platform.getServer().getPlayer(playerId);
            if (player != null && !player.isDead()) {
                for (VirtualItem item : new ArrayList<>(activators.keySet())) {
                    List<C> contexts = contexts(player, item);
                    if (contexts.isEmpty()) continue;
                    current.add(item);
                    if (repeat || !previous.contains(item)) activate(item, contexts);
                }
            }
            if (current.isEmpty()) {
                if (possession != null) {
                    possession.cancel();
                    possessions.remove(playerId);
                }
                return;
            }
            if (possession == null) {
                possession = new Possession();
                possession.task = schedule(playerId);
                possessions.put(playerId, possession);
            }
            possession.items = current;
        }

        private void activate(@NotNull VirtualItem item, @NotNull List<C> contexts) {
            List<A> group = activators.get(item);
            if (group == null) return;
            List<A> snapshot = List.copyOf(group);
            for (C context : contexts) {
                List<A> matched = new ArrayList<>(snapshot.size());
                for (A activator : snapshot) {
                    if (matches(activator, context)) matched.add(activator);
                }
                ActivatorsManager.executeLogic(context, matched);
            }
        }

        private @Nullable BukkitTask schedule(@NotNull UUID playerId) {
            if (recheck <= 0) return null;
            long period = 20L * recheck;
            return platform.getServer().getScheduler().runTaskTimer(
                    platform.getPlugin(),
                    () -> update(playerId, true),
                    period,
                    period
            );
        }
    }

    private static final class Possession {
        private Set<VirtualItem> items = Set.of();
        private @Nullable BukkitTask task;

        private void cancel() {
            if (task != null) task.cancel();
        }
    }
}
