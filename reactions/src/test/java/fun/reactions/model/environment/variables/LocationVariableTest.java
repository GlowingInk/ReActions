package fun.reactions.model.environment.variables;

import fun.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class LocationVariableTest {
    private World mockWorld() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        return world;
    }

    @Test
    public void getAndResolvesChildrenTest() {
        Location location = new Location(mockWorld(), 1, 2, 3, 90f, 45f);
        LocationVariable var = new LocationVariable(location);

        assertEquals(var.get(), LocationUtils.locationToString(location));
        assertEquals(var.resolve("x"), "1");
        assertEquals(var.resolve("y"), "2");
        assertEquals(var.resolve("z"), "3");
        assertEquals(var.resolve("world"), "world");
        assertEquals(var.resolve("yaw"), "90.0");
        assertEquals(var.resolve("pitch"), "45.0");
    }

    @Test
    public void zeroDirectionMatchesBlockFormatTest() {
        Location location = new Location(mockWorld(), 1, 2, 3);
        assertEquals(new LocationVariable(location).get(), "world,1,2,3"); // no yaw/pitch

        Block block = mock(Block.class);
        when(block.getLocation()).thenReturn(location);
        LocationVariable fromBlock = new LocationVariable(block);
        assertEquals(fromBlock.get(), "world,1,2,3");
        assertEquals(fromBlock.resolve("x"), "1");
    }
}
