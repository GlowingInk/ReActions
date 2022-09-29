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
import me.fromgate.reactions.selectors.SelectorsManager;
import me.fromgate.reactions.time.Delayer;
import me.fromgate.reactions.time.TimersManager;
import me.fromgate.reactions.time.waiter.WaitingManager;
import me.fromgate.reactions.util.message.BukkitMessenger;
import me.fromgate.reactions.util.message.Msg;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class ReActionsPlugin extends JavaPlugin implements ReActions.Platform {

    private ActivitiesRegistry activitiesRegistry;
    private ActivatorTypesRegistry typesRegistry;
    private ActivatorsManager activatorsManager;
    private PlaceholdersManager placeholdersManager;
    private VariablesManager variablesManager;
    private SelectorsManager selectorsManager;
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
        modulesRegistry.loadModules();
    }

    @Override
    public void onEnable() {
        // TODO god why
        Msg.init("ReActions", new BukkitMessenger(this), Cfg.language, Cfg.debugMode, Cfg.languageSave);
        getDataFolder().mkdirs();

        Commander.init(this);
        TimersManager.init();
        FakeCommander.init();
        Externals.init();
        RaVault.init();
        WaitingManager.init();
        Delayer.load();
        if (!Cfg.playerSelfVarFile) variablesManager.load();
        else variablesManager.loadVars();
        LocationHolder.loadLocs();
        SQLManager.init();
        InventoryMenu.init();
        Bukkit.getLogger().addHandler(new LogHandler());
        Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);
        Bukkit.getPluginManager().registerEvents(new RaListener(), this);
        MoveListener.init();
        GodModeListener.init();
        new Metrics(this, 1894);
        getServer().getScheduler().runTask(this, () -> {
            modulesRegistry.registerPluginDepended();
            activatorsManager.loadGroup("", false);
        });
    }

    @Override
    public Logger logger() {
        return getSLF4JLogger();
    }

    @Override
    public ActivatorTypesRegistry getActivatorTypes() {
        return typesRegistry;
    }

    @Override
    public ActivatorsManager getActivators() {
        return activatorsManager;
    }

    @Override
    public ActivitiesRegistry getActivities() {
        return activitiesRegistry;
    }

    @Override
    public PlaceholdersManager getPlaceholders() {
        return placeholdersManager;
    }

    @Override
    public VariablesManager getVariables() {
        return variablesManager;
    }

    @Override
    public SelectorsManager getSelectors() {
        return selectorsManager;
    }

    @Override
    public Plugin getPlugin() {
        return this;
    }

    @Override
    public ModulesRegistry getModules() {
        return modulesRegistry;
    }
}
