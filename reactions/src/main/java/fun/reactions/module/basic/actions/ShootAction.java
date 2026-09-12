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

package fun.reactions.module.basic.actions;

import fun.reactions.cfg.RaConfiguration;
import fun.reactions.cfg.Reloadable;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.ContextManager;
import fun.reactions.util.Rng;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.message.Msg;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.parameter.Parameters;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// TODO Make from scratch?
public class ShootAction implements Action, Activity.Personal, Reloadable, Listener {
    private final Object2IntMap<UUID> shotTargets = new Object2IntOpenHashMap<>();

    private Set<Material> breakTypes = EnumSet.noneOf(Material.class);
    private Set<Material> throughTypes = EnumSet.noneOf(Material.class);

    @Override
    public void acceptReload(@NotNull RaConfiguration config) {
        RaConfiguration.ShootCfg shoot = config.actionsCfg().shoot();

        Set<Material> newBreakTypes = EnumSet.noneOf(Material.class);
        for (String typeStr : shoot.breakBlock().split(",")) {
            Material type = ItemUtils.getMaterial(typeStr);
            if (type != null) newBreakTypes.add(type);
        }
        breakTypes = newBreakTypes;

        Set<Material> newThroughTypes = EnumSet.noneOf(Material.class);
        for (String typeStr : shoot.penetrable().split(",")) {
            Material type = ItemUtils.getMaterial(typeStr);
            if (type != null) newThroughTypes.add(type);
        }
        throughTypes = newThroughTypes;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        boolean onehit = params.getBoolean("singlehit", true);
        int distance = params.getInteger("distance", 100);
        float knockbackTarget = params.getInteger("knockbackTarget");
        for (LivingEntity victim : getEntityBeam(player, getBeam(player, distance), onehit)) {
            double damage = Rng.nextIntRanged(params.getString("damage", "1"));
            shotTargets.computeInt(victim.getUniqueId(), (_, count) -> count == null ? 1 : count + 1);
            victim.damage(damage, player);
            if (shotTargets.computeIfPresent(victim.getUniqueId(), (_, count) -> count == 1 ? null : count - 1) != null) {
                Vector eVec = victim.getLocation().toVector().clone();
                Vector dVec = player.getLocation().toVector().clone();
                Vector eDirection = eVec.subtract(dVec).normalize();
                eDirection.add(new Vector(0.0D, 0.1D, 0.0D)).multiply(knockbackTarget);
                victim.setVelocity(eDirection);

                if (params.contains("run")) {
                    executeActivator(player, victim, params.getString("run"));
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "SHOOT";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (event.isCancelled()) {
            shotTargets.computeIntIfPresent(entity.getUniqueId(), (id, count) -> count == 1 ? null : count - 1);
        }
    }

    private void executeActivator(Player shooter, LivingEntity target, String paramStr) {
        Parameters param = Parameters.fromString(paramStr);
        if (param.isEmpty() || !param.containsAny("activator", "exec")) return;
        Player player = target instanceof Player ? (Player) target : null;
        if (player == null && param.getBoolean("playeronly", true)) return;
        param = param.with("player", player == null ? "~null" : player.getName());
        Map<String, Variable> vars = new HashMap<>();
        vars.put("targettype", Variable.value(target.getType()));
        vars.put("targetname", Variable.value(EntityUtils.getEntityDisplayName(target)));
        vars.put("targetloc", Variable.value(LocationUtils.locationToString(target.getLocation())));
        if (shooter != null) {
            vars.put("shooter", Variable.value(shooter.getName()));
            vars.put("shooterloc", Variable.value(LocationUtils.locationToString(shooter.getLocation())));
        }
        ContextManager.triggerFunction(shooter, param, new Variables(vars));
    }

    private List<Block> getBeam(LivingEntity p, int distance) {
        List<Block> beam = new ArrayList<>();
        BlockIterator bi = new BlockIterator(p, distance);
        while (bi.hasNext()) {
            Block b = bi.next();
            if (isEmpty(b, p)) beam.add(b);
            else break;
        }
        return beam;
    }

    private Set<LivingEntity> getEntityBeam(LivingEntity shooter, List<Block> beam, boolean hitSingle) {
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

    private boolean isEmpty(Block b, LivingEntity shooter) {
        if (!b.getType().isCollidable()) return true;
        if (!throughTypes.contains(b.getType())) return true;
        if ((shooter instanceof Player) && (isShotAndBreak(b, (Player) shooter))) {
            b.getWorld().playEffect(b.getLocation(), Effect.DESTROY_BLOCK, b.getType());
            b.breakNaturally();
            return true;
        }
        return false;
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean isShotAndBreak(Block b, Player p) {
        if (breakTypes.contains(b.getType())) {
            try {
                BlockBreakEvent event = new BlockBreakEvent(b, p);
                Bukkit.getPluginManager().callEvent(event);
                return !event.isCancelled();
            } catch (NoSuchMethodError er) {
                Msg.logOnce("bbevent", "BlockBreakEvent constructor is no longer valid. Warn a developer.");
            }
        }
        return false;
    }

    private static boolean isEntityAffectByBeamBlock(Block b, LivingEntity le) {
        if (le.getLocation().getBlock().equals(b)) return true;
        return le.getEyeLocation().getBlock().equals(b);
    }
}
