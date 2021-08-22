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

package me.fromgate.reactions.externals;

import lombok.experimental.UtilityClass;
import me.fromgate.reactions.util.message.Msg;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
@Deprecated
public class RaVault {
    private boolean vault_perm = false;
    private boolean vault_eco = false;
    private Permission permission = null;
    private Economy economy = null;

    public void init() {
        if (checkVault()) {
            vault_perm = setupPermissions();
            vault_eco = setupEconomy();
            Msg.logMessage("Vault connected");
        }
    }

    public boolean isEconomyConnected() {
        return vault_eco;
    }

    public boolean isPermissionConnected() {
        return vault_perm;
    }


    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }


    @Deprecated
    public double getBalance(String account) {
        if (!isEconomyConnected()) return 0;
        return economy.getBalance(account);
    }

    @Deprecated
    public void withdrawPlayer(String account, double amount) {
        if (!isEconomyConnected()) return;
        economy.withdrawPlayer(account, amount);
    }

    @Deprecated
    public void depositPlayer(String account, double amount) {
        if (!isEconomyConnected()) return;
        economy.depositPlayer(account, amount);
    }

    public boolean playerAddGroup(Player p, String group) {
        if (!isPermissionConnected()) return false;
        return permission.playerAddGroup(p, group);
    }

    public boolean playerInGroup(Player p, String group) {
        if (!isPermissionConnected()) return false;
        return permission.playerInGroup(p, group);
    }

    public boolean playerRemoveGroup(Player p, String group) {
        if (!isPermissionConnected()) return false;
        return permission.playerRemoveGroup(p, group);
    }

    private boolean checkVault() {
        Plugin vplg = Bukkit.getPluginManager().getPlugin("Vault");
        return vplg != null;
    }

    private void depositAccount(String account, String worldName, double amount) {
        if (worldName.isEmpty()) economy.depositPlayer(account, amount);
        else economy.depositPlayer(account, worldName, amount);
    }

    private void withdrawAccount(String account, String worldName, double amount) {
        if (worldName.isEmpty()) economy.withdrawPlayer(account, amount);
        else economy.withdrawPlayer(account, worldName, amount);
    }


    /*
     * New method
     */
    public boolean hasMoney(String account, String worldName, double amount) {
        if (!RaVault.isEconomyConnected()) return false;
        if (worldName.isEmpty()) return economy.has(account, amount);
        if (Bukkit.getWorld(worldName) == null) return false;
        return economy.has(account, worldName, amount);
    }


    public boolean creditAccount(String target, String source, double amount, String worldName) {
        if (!RaVault.isEconomyConnected()) return false;
        if (!source.isEmpty()) {
            if (hasMoney(source, worldName, amount)) return false;
            withdrawAccount(source, worldName, amount);
        }
        depositAccount(target, worldName, amount);
        return true;
    }

    public boolean debitAccount(String accountFrom, String accountTo, double amount, String worldName) {
        if (!RaVault.isEconomyConnected()) return false;
        if (!hasMoney(accountFrom, worldName, amount)) return false;
        withdrawAccount(accountFrom, worldName, amount);
        if (!accountTo.isEmpty()) depositAccount(accountTo, worldName, amount);
        return true;
    }

    /*
     * worldName ignored. Vault is not supporting formatting for world's vau
     */
    public String format(double amount, String worldName) {
        if (!isEconomyConnected()) return Double.toString(amount);
        if (worldName.equalsIgnoreCase(Bukkit.getWorlds().get(0).getName())) return Double.toString(amount);
        return economy.format(amount);
    }

    @Deprecated
    public String formatMoney(String value) {
        if (!isEconomyConnected()) return value;
        return economy.format(Double.parseDouble(value)); // Integer???
    }

    public Map<String, String> getAllBalances(String name) {
        Map<String, String> bals = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            String key = "money." + world.getName();
            String amount = format(economy.getBalance(name, world.getName()), world.getName());
            bals.put(key, amount);
            if (Bukkit.getWorlds().get(0).equals(world)) bals.put("money", amount);
        }
        return bals;
    }

}
