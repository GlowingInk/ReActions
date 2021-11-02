package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

// TODO
public class ProjectileHitStorage extends Storage {

    private final EntityType projType;
    private final Block block;
    private final BlockFace blockFace;
    private final Entity entity;
    private final boolean entityHit;

    public ProjectileHitStorage(Player player, EntityType projType, Block block, BlockFace face, Entity entity) {
        super(player);
        this.projType = projType;
        this.entityHit = entity != null;
        this.block = block;
        this.blockFace = face;
        this.entity = entity;
    }

    @Override
    public Class<? extends Activator> getType() {
        return ProjectileHitActivator.class;
    }

    public EntityType getProjType() {return this.projType;}

    public Block getBlock() {return this.block;}

    public BlockFace getBlockFace() {return this.blockFace;}

    public Entity getEntity() {return this.entity;}

    public boolean isEntityHit() {return this.entityHit;}
}
