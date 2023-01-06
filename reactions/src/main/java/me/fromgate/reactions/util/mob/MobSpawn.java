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

package me.fromgate.reactions.util.mob;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.externals.worldguard.RaWorldGuard;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Rng;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import me.fromgate.reactions.util.parameter.ParametersUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MobSpawn { // TODO: Requires major refactoring

    private static final Map<LivingEntity, List<ItemStack>> drops = new HashMap<>();

    private MobSpawn() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static void mobSpawn(Player p, Parameters params) {
        String mob = params.getString("type", "").toUpperCase(Locale.ROOT);
        if (mob.isEmpty()) {
            Msg.logMessage("Failed to spawn mob: " + params);
            return;
        }
        String locationStr = params.getString("loc", "");
        Location loc = LocationUtils.parseLocation(locationStr, p == null ? null : p.getLocation());
        String region = params.getString("region", "");
        int radius = params.getInteger("radius");
        int num = Rng.nextIntRanged(params.getString("num", "1"));
        double health = Rng.nextIntRanged(params.getString("health", "0"));
        String dtheffect = params.getString("dtheffect", "");
        String chest = params.getString("chest", "");
        String leg = params.getString("leg", "");
        String helm = params.getString("helm", "");
        String boot = params.getString("boot", "");
        String weapon = params.getString("weapon", "");
        String offhand = params.getString("offhand", "");
        boolean land = params.getBoolean("land", true);
        String potionEff = params.getString("potion", "");
        String name = params.getString("name", "");
        String drop = params.getString("drop", "");
        String xp = params.getString("xp", "");
        String money = params.getString("money", "");
        String growl = params.getString("growl", "");
        String cry = params.getString("cry", "");
        String equip = params.getString("equip", "");
        double dmg = params.getDouble("dmg", 1.0D);
        String exec = params.getString("run", "");
        String exec_delay = params.getString("rundelay", "1t");

        if (RaWorldGuard.isRegionExists(region)) loc = LocationUtils.getRegionLocation(region, land);
        else if (radius > 0) loc = LocationUtils.getRadiusLocation(loc, radius, land);
        if (loc == null) return;

        for (int i = 0; i < num; i++) {
            List<LivingEntity> mobs = spawnMob(loc, mob);
            for (LivingEntity le : mobs) {
                setMobHealth(le, health);
                setMobName(le, name);
                potionEffect(le, potionEff);
                if (equip.isEmpty()) setMobEquipment(le, helm, chest, leg, boot, weapon, offhand);
                else setMobEquipment(le, equip);
                setMobDrop(le, drop);
                setMobXP(le, xp);
                setMobMoney(le, money);
                setMobExec(le, exec, exec_delay);
                setMobDmgMultiplier(le, dmg);
                setMobGrowl(le, growl);
                setMobCry(le, cry);
                setDeathEffect(le, dtheffect);
            }
        }
    }


    private static List<LivingEntity> spawnMob(Location loc, String mobstr) {
        List<LivingEntity> mobs = new ArrayList<>();
        String[] ln = mobstr.split(":");
        if (ln.length < 1) return mobs;

        //for (int i = 0; i < Math.min(2, ln.length); i++) {
        int k = 0;
        for (String mbs : ln) {
            String name = "";
            if (mbs.contains("$")) {
                name = mbs.substring(0, mbs.indexOf("$"));
                mbs = mbs.substring(name.length() + 1);
            }

            EntityType et = Utils.getEnum(EntityType.class, mbs, EntityType.ZOMBIE);

            Entity e = loc.getWorld().spawnEntity(loc, et);

            if (!(e instanceof LivingEntity mob)) {
                e.remove();
                Msg.logOnce("mobspawnnotmob_" + mobstr, "Cannot spawn mob " + mbs + " (" + mobstr + ")");
                continue;
            }

            setMobName(mob, name);
            mobs.add(mob);
            if (k > 0) mobs.get(k).addPassenger(mobs.get(k - 1));
            k++;
        }
        //if (mobs.size() == 2) mobs.get(1).setPassenger(mobs.get(0));
        return mobs;
    }

    private static void setMobName(LivingEntity e, String name) {
        if (name.isEmpty()) return;
        if ((e.getCustomName() != null) && (!e.getCustomName().isEmpty())) return;
        e.setCustomName(ChatColor.translateAlternateColorCodes('&', name.replace("_", " ")));
        e.setCustomNameVisible(true);
    }

    private static void setMobXP(LivingEntity e, String xp) {
        if (xp.isEmpty()) return;
        e.setMetadata("ReActions-xp", new FixedMetadataValue(ReActions.getPlugin(), xp));
    }

    private static void setMobMoney(LivingEntity e, String money) {
        if (money.isEmpty()) return;
        e.setMetadata("ReActions-money", new FixedMetadataValue(ReActions.getPlugin(), money));
    }

    private static void setMobExec(LivingEntity e, String exec_activator, String exec_delay) {
        if (exec_activator.isEmpty()) return;
        e.setMetadata("ReActions-activator", new FixedMetadataValue(ReActions.getPlugin(), "activator:" + exec_activator + (exec_delay.isEmpty() ? "" : " delay:" + exec_delay)));
    }

    private static void setMobDrop(LivingEntity e, String drop) {
        //id:data*amount,id:dat*amount%chance;id:data*amount;id:dat*amount%chance;id:data*amount;id:dat*amount%chance
        if (drop.isEmpty()) return;
        List<ItemStack> stack = ItemUtils.parseRandomItemsStr(drop);
        if (stack.isEmpty()) return;
        setMobDropStack(e, stack);
    }

    private static void setMobDmgMultiplier(LivingEntity e, double dmg) {
        if (dmg < 0) return;
        e.setMetadata("ReActions-dmg", new FixedMetadataValue(ReActions.getPlugin(), dmg));
    }

    private static void setMobCry(LivingEntity e, String cry) {
        if (cry.isEmpty()) return;
        e.setMetadata("ReActions-cry", new FixedMetadataValue(ReActions.getPlugin(), cry));
    }

    private static void setMobGrowl(LivingEntity e, String growl) {
        if (growl.isEmpty()) return;
        e.setMetadata("ReActions-growl", new FixedMetadataValue(ReActions.getPlugin(), growl));
    }

    private static void setMobDropStack(LivingEntity e, List<ItemStack> stack) {
        if (stack.isEmpty()) return;
        drops.put(e, stack);
        //e.setMetadata("ReActions-drop", new FixedMetadataValue(ReActions.getPlugin(), stack));
    }

    private static void setDeathEffect(LivingEntity e, String dtheffect) {
        if (dtheffect.isEmpty()) return;
        e.setMetadata("ReActions-deatheffect", new FixedMetadataValue(ReActions.getPlugin(), dtheffect));
    }


    private static void setMobHealth(LivingEntity e, double health) {
        if (health > 0) {
            if (health > e.getHealth())
                e.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        } else health = e.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        e.setHealth(health);
    }

    private static void setMobEquipment(LivingEntity e, String equip) {
        if (equip.isEmpty()) return;
        if (!Utils.isWordInList(e.getType().name(), "zombie,skeleton")) return;
        String[] ln = equip.split(";");
        if (ln.length == 0) return;
        String[] eq = {"", "", "", "", "", ""};
        System.arraycopy(ln, 0, eq, 0, Math.min(ln.length, 6));
        setMobEquipment(e, eq[0], eq[1], eq[2], eq[3], eq[4], eq[5]);
    }

    private static void setMobEquipment(LivingEntity e, String helm, String chest, String leg, String boot, String weapon, String offhand) {
        // if (!Util.isWordInList(e.getType().name(), "zombie,skeleton,villager")) return;
        if (!helm.isEmpty()) {
            ItemStack item = getRandomItem(helm);
            e.getEquipment().setHelmet(item);
        }
        if (!chest.isEmpty()) {
            ItemStack item = getRandomItem(chest);
            e.getEquipment().setChestplate(item);
        }
        if (!leg.isEmpty()) {
            ItemStack item = getRandomItem(leg);
            e.getEquipment().setLeggings(item);
        }
        if (!boot.isEmpty()) {
            ItemStack item = getRandomItem(boot);
            e.getEquipment().setBoots(item);
        }
        if (!weapon.isEmpty()) {
            ItemStack item = getRandomItem(weapon);
            e.getEquipment().setItemInMainHand(item);
        }

        if (!offhand.isEmpty()) {
            ItemStack item = getRandomItem(offhand);
            e.getEquipment().setItemInOffHand(item);
        }
    }

    private static void potionEffect(LivingEntity e, String potion) {
        if (potion.isEmpty()) return;
        String[] pts = potion.split(",");
        for (String pot : pts) {
            String pef;
            int level = 1;
            String[] ln = pot.split(":");
            pef = ln[0];
            PotionEffectType pet = PotionEffectType.getByName(pef);
            if (pet == null) continue;
            if ((ln.length == 2) && NumberUtils.isPositiveInt(ln[1])) level = Integer.parseInt(ln[1]);
            PotionEffect pe = new PotionEffect(pet, Integer.MAX_VALUE, level, true);
            e.addPotionEffect(pe);
        }
    }

    public static List<ItemStack> getMobDrop(LivingEntity le) {
        if (drops.containsKey(le)) {
            List<ItemStack> drop = drops.get(le);
            drops.remove(le);
            return drop;
        } else {
            List<LivingEntity> deadMobs = new ArrayList<>();
            for (LivingEntity l : drops.keySet())
                if (l.isDead()) deadMobs.add(l);
            for (LivingEntity l : deadMobs)
                drops.remove(l);
        }
        return null;
    }

    private static ItemStack getRandomItem(String str) {
        if (str.isEmpty()) return new ItemStack(Material.AIR);
        List<String> ln = ParametersUtils.splitSafely(str, ',');
        ItemStack item = VirtualItem.asItem(Rng.randomElement(ln));
        if (item == null) return new ItemStack(Material.AIR);
        item.setAmount(1);
        return item;
    }
}
