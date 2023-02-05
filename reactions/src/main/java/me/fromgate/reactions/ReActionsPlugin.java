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

package me.fromgate.reactions;

import me.fromgate.reactions.commands.Commander;
import me.fromgate.reactions.commands.custom.FakeCommander;
import me.fromgate.reactions.events.listeners.BukkitListener;
import me.fromgate.reactions.events.listeners.GodModeListener;
import me.fromgate.reactions.events.listeners.LogHandler;
import me.fromgate.reactions.events.listeners.MoveListener;
import me.fromgate.reactions.events.listeners.RaListener;
import me.fromgate.reactions.externals.Externals;
import me.fromgate.reactions.externals.RaVault;
import me.fromgate.reactions.holders.LocationHolder;
import me.fromgate.reactions.logic.activators.ActivatorTypesRegistry;
import me.fromgate.reactions.logic.activators.ActivatorsManager;
import me.fromgate.reactions.logic.activity.ActivitiesRegistry;
import me.fromgate.reactions.menu.InventoryMenu;
import me.fromgate.reactions.module.ModulesRegistry;
import me.fromgate.reactions.module.basics.BasicModule;
import me.fromgate.reactions.placeholders.LegacyPlaceholdersManager;
import me.fromgate.reactions.placeholders.ModernPlaceholdersManager;
import me.fromgate.reactions.placeholders.PlaceholdersManager;
import me.fromgate.reactions.save.SavingManager;
import me.fromgate.reactions.selectors.SelectorsManager;
import me.fromgate.reactions.time.LazyDelayManager;
import me.fromgate.reactions.time.timers.TimersManager;
import me.fromgate.reactions.time.wait.WaitingManager;
import me.fromgate.reactions.util.message.Messenger;
import me.fromgate.reactions.util.message.Msg;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ReActionsPlugin extends JavaPlugin implements ReActions.Platform {

    private ActivitiesRegistry activitiesRegistry;
    private ActivatorTypesRegistry typesRegistry;
    private ActivatorsManager activatorsManager;
    private PlaceholdersManager placeholdersManager;
    private VariablesManager variablesManager;
    private SelectorsManager selectorsManager;
    private WaitingManager waitingManager;
    private SavingManager savingManager;
    private ModulesRegistry modulesRegistry;

    @Override
    public void onLoad() {
        Cfg.load(getConfig());
        Cfg.save(getConfig());
        saveConfig();
        this.variablesManager = new VariablesManager();
        if (Cfg.modernPlaceholders) {
            this.placeholdersManager = new ModernPlaceholdersManager();
        } else {
            logger().warn("You're using legacy placeholders parser! " +
                    "While it does protect compatibility, it's highly recommended to use modern one. " +
                    "Turn it on by enabling 'use-modern-placeholders' in the config (restart required).");
            this.placeholdersManager = new LegacyPlaceholdersManager();
        }
        this.activitiesRegistry = new ActivitiesRegistry();
        this.typesRegistry = new ActivatorTypesRegistry(this);
        this.activatorsManager = new ActivatorsManager(this, activitiesRegistry, typesRegistry);
        this.selectorsManager = new SelectorsManager();
        this.modulesRegistry = new ModulesRegistry(this);
        ReActions.setPlatform(this);
        modulesRegistry.registerModule(new BasicModule());
        modulesRegistry.loadFolderModules();
    }

    @Override
    public void onEnable() {
        this.waitingManager = new WaitingManager(this);
        this.savingManager = new SavingManager(this);
        savingManager.register(waitingManager);
        waitingManager.load();

        // TODO god why
        Msg.init("ReActions", new Messenger(this), Cfg.language, Cfg.debugMode, Cfg.languageSave);
        getDataFolder().mkdirs();

        Commander.init(this);
        TimersManager.init();
        FakeCommander.init(this);
        Externals.init();
        RaVault.init();
        LazyDelayManager.load();
        if (!Cfg.playerSelfVarFile) variablesManager.load();
        else variablesManager.loadVars();
        LocationHolder.loadLocs();
        SQLManager.init();
        InventoryMenu.init(this);
        getServer().getLogger().addHandler(new LogHandler());
        getServer().getPluginManager().registerEvents(savingManager, this);
        getServer().getPluginManager().registerEvents(new BukkitListener(), this);
        getServer().getPluginManager().registerEvents(new RaListener(), this);
        MoveListener.init();
        GodModeListener.init();
        new Metrics(this, 1894);
        getServer().getScheduler().runTask(this, () -> {
            modulesRegistry.registerPluginDepended();
            activatorsManager.loadGroup("", false);
        });
    }

    @Override
    public @NotNull Logger logger() {
        return getSLF4JLogger();
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
    public @NotNull PlaceholdersManager getPlaceholders() {
        return placeholdersManager;
    }

    @Override
    public @NotNull VariablesManager getVariables() {
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
