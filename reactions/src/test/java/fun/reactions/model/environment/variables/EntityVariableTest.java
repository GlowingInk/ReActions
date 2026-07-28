package fun.reactions.model.environment.variables;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class EntityVariableTest {
    @Test
    public void getAndResolvesChildrenIntoLocationTest() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");

        Entity entity = mock(Entity.class);
        when(entity.getType()).thenReturn(EntityType.ZOMBIE);
        when(entity.getCustomName()).thenReturn("Oleg");
        when(entity.getWorld()).thenReturn(world);
        when(entity.getLocation()).thenReturn(new Location(world, 1, 2, 3));

        EntityVariable var = new EntityVariable(entity);
        assertEquals(var.get(), "ZOMBIE");
        assertEquals(var.resolve("x"), "1");
        assertEquals(var.resolve("type"), "ZOMBIE");
        assertEquals(var.resolve("name"), "Oleg");
        assertEquals(var.resolve("world"), "world");
        assertEquals(var.resolve("location:x"), "1");
        assertEquals(var.resolve("location:world"), "world");
    }
}
