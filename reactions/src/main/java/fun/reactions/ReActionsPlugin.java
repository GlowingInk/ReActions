/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *
 *  This file is part of ReActions.
 *
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package fun.reactions;

import fun.reactions.commands.impl.ActivatorSub;
import fun.reactions.commands.impl.CreateSub;
import fun.reactions.commands.nodes.StringArgNode;
import fun.reactions.commands.user.UserCommandsManager;
import fun.reactions.events.listeners.BukkitListener;
import fun.reactions.events.listeners.GodModeListener;
import fun.reactions.events.listeners.LogHandler;
import fun.reactions.events.listeners.MoveListener;
import fun.reactions.events.listeners.RaListener;
import fun.reactions.holders.LocationHolder;
import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activators.type.ActivatorTypesRegistry;
import fun.reactions.model.activity.ActivitiesRegistry;
import fun.reactions.module.ModulesRegistry;
import fun.reactions.module.basic.BasicModule;
import fun.reactions.module.papi.PapiModule;
import fun.reactions.module.vault.VaultModule;
import fun.reactions.module.worldedit.WorldEditModule;
import fun.reactions.module.worldguard.WorldGuardModule;
import fun.reactions.placeholders.LegacyPlaceholdersManager;
import fun.reactions.placeholders.ModernPlaceholdersManager;
import fun.reactions.placeholders.PlaceholdersManager;
import fun.reactions.save.SavingManager;
import fun.reactions.selectors.SelectorsManager;
import fun.reactions.time.CooldownManager;
import fun.reactions.time.timers.TimersManager;
import fun.reactions.time.wait.WaitingManager;
import fun.reactions.util.message.Messenger;
import fun.reactions.util.message.Msg;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static fun.reactions.commands.nodes.CommandNode.command;
import static fun.reactions.commands.nodes.LiteralNode.literal;
import static fun.reactions.commands.nodes.StringArgNode.stringArg;

public class ReActionsPlugin extends JavaPlugin implements ReActions.Platform {
    private ActivitiesRegistry activitiesRegistry;
    private ActivatorTypesRegistry typesRegistry;
    private ActivatorsManager activatorsManager;
    private UserCommandsManager userCommandsManager;
    private PlaceholdersManager placeholdersManager;
    private PersistentVariablesManager variablesManager;
    private SelectorsManager selectorsManager;
    private WaitingManager waitingManager;
    private SavingManager savingManager;
    private ModulesRegistry modulesRegistry;

    private LogHandler logHandler; // TODO Move to ContextManager

    @Override
    public void onLoad() {
        Cfg.load(getConfig());
        Cfg.save(getConfig());
        saveConfig();
        this.variablesManager = new PersistentVariablesManager();
        if (Cfg.modernPlaceholders) {
            this.placeholdersManager = new ModernPlaceholdersManager();
        } else {
            logger().warn(
                    "You're using legacy placeholders parser! " +
                    "While it does protect compatibility, it's highly recommended to use modern one. " +
                    "Turn it on by enabling 'use-modern-placeholders' in the config (restart required)."
            );
            logger().warn(
                    "Do note that changing the parser also changes the syntax. " +
                    "Instead of '%variable%' you will need to use '%[variable]'."
            );
            this.placeholdersManager = new LegacyPlaceholdersManager();
        }
        this.activitiesRegistry = new ActivitiesRegistry();
        this.typesRegistry = new ActivatorTypesRegistry(this);
        this.activatorsManager = new ActivatorsManager(this);
        this.selectorsManager = new SelectorsManager();
        this.modulesRegistry = new ModulesRegistry(this);
        this.waitingManager = new WaitingManager(this);
        this.userCommandsManager = new UserCommandsManager(this);
        ReActions.setPlatform(this);

        modulesRegistry.registerModule(new BasicModule());
        modulesRegistry.registerModule(new VaultModule());
        modulesRegistry.registerModule(new PapiModule());
        modulesRegistry.registerModule(new WorldEditModule());
        modulesRegistry.registerModule(new WorldGuardModule());
        modulesRegistry.loadFolderModules();
    }

    @Override
    public void onEnable() {
        // TODO god why
        Msg.init("ReActions", new Messenger(this), Cfg.language, Cfg.debugMode, Cfg.languageSave);

        this.savingManager = new SavingManager(this);
        savingManager.register(waitingManager);
        waitingManager.init();
        userCommandsManager.reload();

        getDataFolder().mkdirs();

        TimersManager.init();
        CooldownManager.load();
        if (!Cfg.playerSelfVarFile) variablesManager.load();
        else variablesManager.loadVars();
        LocationHolder.loadLocs();
        SQLManager.init();
        InventoryMenu.init(this);
        getServer().getLogger().addHandler(logHandler = new LogHandler());
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(savingManager, this);
        pluginManager.registerEvents(new BukkitListener(), this);
        pluginManager.registerEvents(new RaListener(), this);
        pluginManager.registerEvents(userCommandsManager, this);
        MoveListener.init();
        GodModeListener.init();
        Metrics metrics = new Metrics(this, 19363);
        manageCommands();
        metrics.addCustomChart(new SimplePie("placeholders_manager", () -> Cfg.modernPlaceholders ? "Modern" : "Legacy"));
        getServer().getScheduler().runTask(this, () -> {
            modulesRegistry.registerPluginDepended();
            activatorsManager.loadGroup("", false);
        });
    }

    private void manageCommands() {
        Commodore commodore = CommodoreProvider.getCommodore(this);
        PluginCommand reactionsCommand = Objects.requireNonNull(getCommand("reactions"));

        command(commodore, reactionsCommand, (p, s) -> s.sendMessage("help"),
                new CreateSub(this).asNode(),
                new ActivatorSub(this).asNode(),
                literal("location", (p, s) -> s.sendMessage("loc help"),
                        stringArg("name", StringArgNode.Type.WORD, (p, s) -> s.sendMessage("loc specific help"),
                                literal("info", (p, s) -> s.sendMessage("loc info")),
                                literal("delete", (p, s) -> s.sendMessage("loc delete")),
                                literal("tp", (p, s) -> s.sendMessage("loc tp")),
                                literal("move", (p, s) -> s.sendMessage("loc move"))
                        )
                ),
                literal("list", (p, s) -> s.sendMessage("list help"),
                        literal("activators", (p, s) -> s.sendMessage("list activators"),
                                stringArg("group", StringArgNode.Type.WORD, (p, s) -> s.sendMessage("list activators " + p))
                        ),
                        literal("locations", (p, s) -> s.sendMessage("list locations"),
                                stringArg("world", StringArgNode.Type.WORD, (p, s) -> s.sendMessage("list locations " + p))
                        ),
                        literal("menus", (p, s) -> s.sendMessage("list menus"))
                ),
                literal("reload", (p, s) -> s.sendMessage("reload all"),
                        stringArg("options", StringArgNode.Type.WORD, (p, s) -> s.sendMessage("reload specific " + p))
                )
        );
    }

    @Override
    public void onDisable() {
        getServer().getLogger().removeHandler(logHandler);
        savingManager.saveSync();
    }

    @Override
    public @NotNull ComponentLogger logger() {
        return getComponentLogger();
    }

    @Override
    public @NotNull ActivatorTypesRegistry getActivatorTypes() {
        return typesRegistry;
    }

    @Override
    public @NotNull ActivatorsManager getActivators() {
        return activatorsManager;
    }

    @Override
    public @NotNull ActivitiesRegistry getActivities() {
        return activitiesRegistry;
    }

    @Override
    public @NotNull UserCommandsManager getCommands() {
        return userCommandsManager;
    }

    @Override
    public @NotNull PlaceholdersManager getPlaceholders() {
        return placeholdersManager;
    }

    @Override
    public @NotNull PersistentVariablesManager getPersistentVariables() {
        return variablesManager;
    }

    @Override
    public @NotNull SelectorsManager getSelectors() {
        return selectorsManager;
    }

    @Override
    public @NotNull WaitingManager getWaiter() {
        return waitingManager;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return this;
    }

    @Override
    public @NotNull ModulesRegistry getModules() {
        return modulesRegistry;
    }
}
