package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;

public class ItemVariableTest {
    @Test
    public void typeAndAmountAreAnsweredWithoutBuildingTheFullStructTest() {
        ItemStack item = mock(ItemStack.class); // hasItemMeta() etc left unstubbed on purpose
        when(item.getType()).thenReturn(Material.DIAMOND_SWORD);
        when(item.getAmount()).thenReturn(1);

        ItemVariable var = new ItemVariable(item);
        assertEquals(var.resolve("type"), "DIAMOND_SWORD");
        assertEquals(var.resolve("amount"), "1");
    }

    @Test
    public void forkReturnsAnIndependentInstanceTest() {
        ItemStack item = mock(ItemStack.class);
        ItemVariable var = new ItemVariable(item);
        Variable forked = var.fork();
        assertNotSame(forked, var);
    }
}
