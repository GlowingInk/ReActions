package fun.reactions.model.environment.variables;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class BlockVariableTest {
    @Test
    public void getAndResolvesChildrenIntoLocationTest() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.STONE);
        when(block.getX()).thenReturn(1);
        when(block.getY()).thenReturn(2);
        when(block.getZ()).thenReturn(3);
        when(block.getWorld()).thenReturn(world);
        when(block.getLocation()).thenReturn(new Location(world, 1, 2, 3));

        BlockVariable var = new BlockVariable(block);
        assertEquals(var.get(), "STONE");
        assertEquals(var.resolve("x"), "1");
        assertEquals(var.resolve("type"), "STONE");
        assertEquals(var.resolve("world"), "world");
        assertEquals(var.resolve("location"), var.child("location").get());
        assertEquals(var.resolve("location:x"), "1");
        assertEquals(var.resolve("location:world"), "world");
    }
}
