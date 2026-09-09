package fun.reactions.util;

import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;

/**
 * Some helpful methods related to blocks to minify size of code
 */
public final class BlockUtils {
    private BlockUtils() {}

    public static boolean isSign(Block block) {
        return Tag.SIGNS.isTagged(block.getType());
    }

    public static boolean setOpen(Block b, boolean open) {
        if (isOpenable(b)) {
            Openable om = (Openable) b.getBlockData();
            om.setOpen(open);
            b.setBlockData(om);
            return true;
        }
        return false;
    }

    public static boolean isOpen(Block b) {
        if (isOpenable(b)) {
            Openable om = (Openable) b.getBlockData();
            return om.isOpen();
        }
        return false;
    }

    public static Block getBottomDoor(Block block) {
        if (block.getBlockData() instanceof Door door) {
            if (door.getHalf() == Bisected.Half.BOTTOM) return block;

            Block bottom = block.getRelative(BlockFace.DOWN);
            if (bottom.getBlockData() instanceof Door bottomDoor && bottomDoor.getHalf() == Bisected.Half.BOTTOM) {
                return bottom;
            }
        }
        return block;
    }

    public static boolean isOpenable(Block b) {
        return b.getBlockData() instanceof Openable;
    }
}
