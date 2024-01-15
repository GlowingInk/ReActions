package fun.reactions;

import fun.reactions.placeholders.PlaceholdersManager;
import fun.reactions.time.wait.WaitingManager;
import fun.reactions.util.Shoot;
import fun.reactions.util.Utils;
import org.bukkit.configuration.file.FileConfiguration;

// TODO: Fully rework. Please.
public class Cfg {

    public static boolean debugMode = false;
    public static boolean saveEmptySections = false;
    public static String language = "english";
    public static boolean languageSave = false;
    public static boolean centerTpCoords = true;
    public static int worldguardRecheck = 2;
    public static int itemHoldRecheck = 2;
    public static int itemWearRecheck = 2;
    public static boolean horizontalPushback = false;
    public static int chatLength = 55;
    public static boolean playerSelfVarFile = false;
    public static boolean playerAsynchSaveSelfVarFile = false;
    public static boolean playerMoveTaskUse = false;
    public static int playerMoveTaskTick = 5;
    public static boolean altOperator = false; // experimental, disabled by default
    public static boolean modernPlaceholders = false;
    public static boolean parseBookPages = false;

    public static void save(FileConfiguration config) {
        config.set("general.language", language);
        config.set("general.debug", debugMode);
        config.set("general.player-self-variable-file", playerSelfVarFile);
        config.set("general.player-asynch-save-self-variable-file", playerAsynchSaveSelfVarFile);
        config.set("general.player-move-event.use-task", playerMoveTaskUse);
        config.set("general.player-move-event.task-tick", playerMoveTaskTick);
        config.set("general.placeholder-limit", 32);
        config.set("general.waiter-hours-limit", 8760L);
        config.set("general.waiter-missing-player-behaviour", "SKIP");
        config.set("general.use-modern-placeholders", modernPlaceholders);
        config.set("general.parse-book-pages", parseBookPages);

        config.set("reactions.save-empty-actions-and-flags-sections", saveEmptySections);
        config.set("reactions.center-player-teleport", centerTpCoords);
        config.set("reactions.region-recheck-delay", worldguardRecheck);
        config.set("reactions.item-hold-recheck-delay", itemHoldRecheck);
        config.set("reactions.item-wear-recheck-delay", itemWearRecheck);
        config.set("reactions.horizontal-pushback-action", horizontalPushback);
        config.set("reactions.default-chat-line-length", chatLength);
        config.set("actions.shoot.break-block", Shoot.actionShootBreak);
        config.set("actions.shoot.penetrable", Shoot.actionShootThrough);
        config.set("actions.cmd_op.proxy-operator", false);
    }

    public static void load(FileConfiguration config) {
        language = config.getString("general.language", "english");
        languageSave = config.getBoolean("general.language-save", false);
        debugMode = config.getBoolean("general.debug", false);
        playerSelfVarFile = config.getBoolean("general.player-self-variable-file", false);
        playerAsynchSaveSelfVarFile = config.getBoolean("general.player-asynch-save-self-variable-file", false);
        playerMoveTaskUse = config.getBoolean("general.player-move-event.use-task", false);
        playerMoveTaskTick = config.getInt("general.player-move-event.task-tick", 5);

        WaitingManager.setHoursLimit(config.getLong("general.waiter-hours-limit", 4380));
        WaitingManager.setBehaviour(Utils.getEnum(config.getString("general.waiter-missing-player-behaviour", "SKIP"), WaitingManager.AttachedBehaviour.SKIP));

        PlaceholdersManager.setCountLimit(config.getInt("general.placeholder-limit", 127));
        modernPlaceholders = config.getBoolean("general.use-modern-placeholders", false);

        parseBookPages = config.getBoolean("general.parse-book-pages", false);

        chatLength = config.getInt("reactions.default-chat-line-length", 55);
        saveEmptySections = config.getBoolean("reactions.save-empty-actions-and-flags-sections", false);
        centerTpCoords = config.getBoolean("reactions.center-player-teleport", true);
        worldguardRecheck = config.getInt("reactions.region-recheck-delay", 2);
        itemHoldRecheck = config.getInt("reactions.item-hold-recheck-delay", 2);
        itemWearRecheck = config.getInt("reactions.item-wear-recheck-delay", 2);
        horizontalPushback = config.getBoolean("reactions.horizontal-pushback-action", false);
        Shoot.actionShootBreak = config.getString("actions.shoot.break-block", Shoot.actionShootBreak);
        Shoot.actionShootThrough = config.getString("actions.shoot.penetrable", Shoot.actionShootThrough);
        Shoot.reload();
        altOperator = config.getBoolean("actions.cmd_op.proxy-operator", false);
    }
}
