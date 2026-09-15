package fun.reactions.cfg;

import fun.reactions.time.wait.WaitingManager;
import fun.reactions.util.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RaConfiguration {
    private final List<Reloadable> subscribers = new ArrayList<>();

    private FileConfiguration config;
    private GeneralCfg general;
    private ReactionsCfg reactions;
    private ActionsCfg actions;
    private MySQLCfg mysql;

    public void load(@NotNull FileConfiguration config) {
        this.config = config;
        general = GeneralCfg.load(config);
        reactions = ReactionsCfg.load(config);
        actions = ActionsCfg.load(config);
        mysql = MySQLCfg.load(config);

        reloadSubscribers();
    }

    public @NotNull FileConfiguration config() {
        return config;
    }

    public void register(@NotNull Reloadable reloadable) {
        subscribers.add(reloadable);
        reloadable.acceptReload(this);
    }

    private void reloadSubscribers() {
        subscribers.forEach(sub -> sub.acceptReload(this));
    }

    public @NotNull RaConfiguration.GeneralCfg generalCfg() {
        return general;
    }

    public @NotNull ReactionsCfg reactionsCfg() {
        return reactions;
    }

    public @NotNull ActionsCfg actionsCfg() {
        return actions;
    }

    public @NotNull RaConfiguration.MySQLCfg mysqlCfg() {
        return mysql;
    }

    public record GeneralCfg(
            boolean debugMode,
            boolean parseBookPages,
            boolean playerSelfVarFile,
            boolean playerAsynchSaveSelfVarFile,
            @NotNull PlaceholdersCfg placeholders,
            @NotNull Waiter waiter,
            @NotNull PlayerMoveEvent playerMoveEvent
    ) {
        static @NotNull RaConfiguration.GeneralCfg load(@NotNull FileConfiguration config) {
            return new GeneralCfg(
                    config.getBoolean("general.debug", false),
                    config.getBoolean("general.parse-book-pages", false),
                    config.getBoolean("general.player-self-variable-file", false),
                    config.getBoolean("general.player-asynch-save-self-variable-file", false),
                    PlaceholdersCfg.load(config),
                    Waiter.load(config),
                    PlayerMoveEvent.load(config)
            );
        }
    }

    public record PlaceholdersCfg(boolean modern, int limit) {
        static @NotNull PlaceholdersCfg load(@NotNull FileConfiguration config) {
            return new PlaceholdersCfg(
                    config.getBoolean("general.use-modern-placeholders", true),
                    config.getInt("general.placeholder-limit", 127)
            );
        }
    }

    public record Waiter(long hoursLimit, @NotNull WaitingManager.AttachedBehaviour behaviour) {
        static @NotNull Waiter load(@NotNull FileConfiguration config) {
            return new Waiter(
                    config.getLong("general.waiter-hours-limit", 4380),
                    Utils.getEnum(config.getString("general.waiter-missing-player-behaviour", "SKIP"), WaitingManager.AttachedBehaviour.SKIP)
            );
        }
    }

    public record PlayerMoveEvent(boolean use, int taskTick) {
        static @NotNull PlayerMoveEvent load(@NotNull FileConfiguration config) {
            return new PlayerMoveEvent(
                    config.getBoolean("general.player-move-event.use-task", false),
                    config.getInt("general.player-move-event.task-tick", 5)
            );
        }
    }

    public record ReactionsCfg(
            boolean saveEmptySections,
            boolean centerTpCoords,
            int worldguardRecheck,
            int itemHoldRecheck,
            int itemWearRecheck,
            boolean horizontalPushback
    ) {
        static @NotNull ReactionsCfg load(@NotNull FileConfiguration config) {
            return new ReactionsCfg(
                    config.getBoolean("reactions.save-empty-actions-and-flags-sections", false),
                    config.getBoolean("reactions.center-player-teleport", true),
                    config.getInt("reactions.region-recheck-delay", 2),
                    config.getInt("reactions.item-hold-recheck-delay", 2),
                    config.getInt("reactions.item-wear-recheck-delay", 2),
                    config.getBoolean("reactions.horizontal-pushback-action", false)
            );
        }
    }

    public record ActionsCfg(boolean altOperator, @NotNull ShootCfg shoot) {
        static @NotNull ActionsCfg load(@NotNull FileConfiguration config) {
            return new ActionsCfg(
                    config.getBoolean("actions.cmd_op.proxy-operator", false),
                    ShootCfg.load(config)
            );
        }
    }

    public record ShootCfg(@NotNull String breakBlock, @NotNull String penetrable) {
        static @NotNull ShootCfg load(@NotNull FileConfiguration config) {
            return new ShootCfg(
                    config.getString("actions.shoot.break-block", "GLASS,THIN_GLASS,STAINED_GLASS,STAINED_GLASS_PANE,GLOWSTONE,REDSTONE_LAMP_OFF,REDSTONE_LAMP_ON"),
                    config.getString("actions.shoot.penetrable", "FENCE,FENCE_GATE,IRON_BARDING,IRON_FENCE,NETHER_FENCE")
            );
        }
    }

    public record MySQLCfg(
            @NotNull String server,
            @NotNull String port,
            @NotNull String database,
            @NotNull String username,
            @NotNull String password,
            @NotNull String codepage
    ) {
        static @NotNull RaConfiguration.MySQLCfg load(@NotNull FileConfiguration config) {
            return new MySQLCfg(
                    config.getString("MySQL.server", "localhost"),
                    config.getString("MySQL.port", "3306"),
                    config.getString("MySQL.database", "ReActions"),
                    config.getString("MySQL.username", "root"),
                    config.getString("MySQL.password", "password"),
                    config.getString("MySQL.codepage", "UTF-8")
            );
        }
    }
}
