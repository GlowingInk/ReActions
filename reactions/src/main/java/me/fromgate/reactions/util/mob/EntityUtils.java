package me.fromgate.reactions.util.mob;

import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static me.fromgate.reactions.util.location.LocationUtils.CHUNK_BITS;

/**
 * Some helpful methods related to entities to minify size of code
 */
public final class EntityUtils {

    private EntityUtils() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    /**
     * Get maximal health of entity
     *
     * @param entity Entity to check
     * @return Maximal health of entity
     */
    public static double getMaxHealth(@NotNull LivingEntity entity) {
        return Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
    }

    /**
     * Get {@link LivingEntity} from {@link ProjectileSource}
     *
     * @param source Original source
     * @return LivingEntity or null if shooter was a block
     */
    public static @Nullable LivingEntity getEntityFromProjectile(@Nullable ProjectileSource source) {
        return source instanceof LivingEntity entity ? entity : null;
    }

    /**
     * Get all entities inside cuboid
     *
     * @param l1 Point of cuboid
     * @param l2 Point of cuboid
     * @return List of entities
     */
    public static @NotNull Collection<@NotNull Entity> getEntities(@NotNull Location l1, @NotNull Location l2) {
        Set<Entity> entities = new HashSet<>();
        if (!l1.getWorld().equals(l2.getWorld())) return entities;
        int xMin = Math.min(l1.getBlockX(), l2.getBlockX());
        int xMax = Math.max(l1.getBlockX(), l2.getBlockX());
        int yMin = Math.min(l1.getBlockY(), l2.getBlockY());
        int yMax = Math.max(l1.getBlockY(), l2.getBlockY());
        int zMin = Math.min(l1.getBlockZ(), l2.getBlockZ());
        int zMax = Math.max(l1.getBlockZ(), l2.getBlockZ());
        for (int xCh = xMin >> CHUNK_BITS, xChMax = xMax >> CHUNK_BITS; xCh <= xChMax; xCh++) {
            for (int zCh = zMin >> CHUNK_BITS, zChMax = zMax >> CHUNK_BITS; zCh <= zChMax; zCh++) {
                for (Entity entity : l1.getWorld().getChunkAt(xCh, zCh).getEntities()) {
                    Location entityLoc = entity.getLocation();
                    double x = entityLoc.getX();
                    double y = entityLoc.getY();
                    double z = entityLoc.getZ();
                    if ((xMin <= x && x <= xMax) && (yMin <= y && y <= yMax) && (zMin <= z && z <= zMax)) {
                        entities.add(entity);
                    }
                }
            }
        }
        return entities;
    }

    public static @Nullable LivingEntity getDamagerEntity(@NotNull EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            Projectile prj = (Projectile) event.getDamager();
            return getEntityFromProjectile(prj.getShooter());
        } else if (event.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
            if (event.getDamager() instanceof ThrownPotion potion) {
                return getEntityFromProjectile(potion.getShooter());
            }
        } else if (event.getDamager() instanceof LivingEntity entity) {
            return entity;
        }
        return null;
    }

    public static @NotNull String getEntityDisplayName(@NotNull Entity entity) {
        if (entity instanceof Player player) {
            return player.getName();
        } else {
            return Optional.ofNullable(entity.getCustomName()).orElse(entity.getType().name());
        }
    }

    public static @NotNull String getMobName(@NotNull Nameable mob) {
        return mob.getCustomName() == null ? "" : mob.getCustomName();
    }

    public static @Nullable LivingEntity getKillerEntity(@Nullable EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent evdmg) {
            if (evdmg.getDamager() instanceof LivingEntity entity) return entity;
            if (evdmg.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                Projectile prj = (Projectile) evdmg.getDamager();
                return getEntityFromProjectile(prj.getShooter());
            }
        }
        return null;
    }

    public static @Nullable Player getKillerPlayer(@Nullable EntityDamageEvent event) {
        return getKillerEntity(event) instanceof Player player ? player : null;
    }
}
