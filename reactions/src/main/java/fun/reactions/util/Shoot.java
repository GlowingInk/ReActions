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

package fun.reactions.util;

import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.ContextManager;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.*;

// TODO Make from scratch
public final class Shoot {

    public static String actionShootBreak = "GLASS,THIN_GLASS,STAINED_GLASS,STAINED_GLASS_PANE,GLOWSTONE,REDSTONE_LAMP_OFF,REDSTONE_LAMP_ON";
    public static String actionShootThrough = "FENCE,FENCE_GATE,IRON_BARDING,IRON_FENCE,NETHER_FENCE";

    private static Set<Material> breakTypes;
    private static Set<Material> throughTypes;

    public static void reload() {
        breakTypes = EnumSet.noneOf(Material.class);
        for (String typeStr : actionShootBreak.split(",")) {
            Material type = ItemUtils.getMaterial(typeStr);
            if (type != null) breakTypes.add(type);
        }
        throughTypes = EnumSet.noneOf(Material.class);
        for (String typeStr : actionShootThrough.split(",")) {
            Material type = ItemUtils.getMaterial(typeStr);
            if (type != null) throughTypes.add(type);
        }
    }

    private Shoot() {}

    public static void shoot(LivingEntity shooter, Parameters params) {
        boolean onehit = params.getBoolean("singlehit", true);
        int distance = params.getInteger("distance", 100);
        float knockbackTarget = params.getInteger("knockbackTarget");
        for (LivingEntity le : getEntityBeam(shooter, getBeam(shooter, distance), onehit)) {
            double damage = Rng.nextIntRanged(params.getString("damage", "1"));
            boolean shoot = true;
            if (damage > 0) {
                shoot = damageEntity(shooter, le, damage, knockbackTarget);
            }
            if (shoot && params.contains("run")) {
                executeActivator(shooter instanceof Player ? (Player) shooter : null, le, params.getString("run"));
            }
        }
    }

    private static void executeActivator(Player shooter, LivingEntity target, String paramStr) {
        Parameters param = Parameters.fromString(paramStr);
        if (param.isEmpty() || !param.containsAny("activator", "exec")) return;
        Player player = target instanceof Player ? (Player) target : null;
        if (player == null && param.getBoolean("playeronly", true)) return;
        param = param.with("player", player == null ? "~null" : player.getName());
        Map<String, Variable> vars = new HashMap<>();
        vars.put("targettype", Variable.simple(target.getType()));
        vars.put("targetname", Variable.simple(EntityUtils.getEntityDisplayName(target)));
        vars.put("targetloc", Variable.simple(LocationUtils.locationToString(target.getLocation())));
        if (shooter != null) {
            vars.put("shooter", Variable.simple(shooter.getName()));
            vars.put("shooterloc", Variable.simple(LocationUtils.locationToString(shooter.getLocation())));
        }
        ContextManager.triggerFunction(shooter, param, new Variables(vars));
    }

    private static List<Block> getBeam(LivingEntity p, int distance) {
        List<Block> beam = new ArrayList<>();
        BlockIterator bi = new BlockIterator(p, distance);
        while (bi.hasNext()) {
            Block b = bi.next();
            if (isEmpty(b, p)) beam.add(b);
            else break;
        }
        return beam;
    }

    private static Set<LivingEntity> getEntityBeam(LivingEntity shooter, List<Block> beam, boolean hitSingle) {
        Set<LivingEntity> list = new HashSet<>();
        for (Block b : beam)
            for (Entity e : b.getChunk().getEntities()) {
                if (!(e instanceof LivingEntity le)) continue;
                if (le.equals(shooter)) continue;
                if (isEntityAffectByBeamBlock(b, le)) {
                    list.add(le);
                    if (hitSingle) return list;
                }
            }
        return list;
    }

    private static boolean isEmpty(Block b, LivingEntity shooter) {
        if (!b.getType().isCollidable()) return true;
        if (!throughTypes.contains(b.getType())) return true;
        if ((shooter instanceof Player) && (isShotAndBreak(b, (Player) shooter))) {
            b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
            b.breakNaturally();
            return true;
        }
        return false;
    }


    public static boolean breakBlock(Block b, Player p) {
        BlockBreakEvent event = new BlockBreakEvent(b, p);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private static boolean isShotAndBreak(Block b, Player p) {
        if (breakTypes.contains(b.getType())) return breakBlock(b, p);
        return false;
    }

    private static boolean isEntityAffectByBeamBlock(Block b, LivingEntity le) {
        if (le.getLocation().getBlock().equals(b)) return true;
        return le.getEyeLocation().getBlock().equals(b);
    }

    public static boolean damageEntity(LivingEntity damager, LivingEntity entity, double damage, float knockbackTarget) {
        Vector eVec = entity.getLocation().toVector().clone();
        Vector dVec = damager.getLocation().toVector().clone();
        Vector eDirection = eVec.subtract(dVec).normalize();
        eDirection.add(new Vector(0.0D, 0.1D, 0.0D)).multiply(knockbackTarget);
        entity.setVelocity(eDirection);

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
                damager, entity,
                DamageCause.ENTITY_ATTACK,
                DamageSource.builder(DamageType.GENERIC).withCausingEntity(damager).build(),
                damage
        );
        Bukkit.getPluginManager().callEvent(event);
        if (!(event.isCancelled()))
            entity.damage(damage);
        return !event.isCancelled();
    }

}
