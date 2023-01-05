package me.fromgate.reactions.util;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class UtilsTest {
    @DataProvider
    public Object[][] cutBuilderData() {
        return new Object[][] {
                {"input", 1, "inpu"},
                {"full", 4, ""},
                {"overflow", 9, ""},
                {"nothing", 0, "nothing"}
        };
    }

    @Test(dataProvider = "cutBuilderData")
    public void cutBuilderTest(String input, int offset, String expected) {
        assertEquals(
                Utils.cutBuilder(new StringBuilder(input), offset),
                expected
        );
    }

    @DataProvider
    public Object[][] getColorData() {
        return new Object[][] {
                {"#FFFFFF", Color.WHITE},
                {"255,0,0", Color.RED},
                {"BLUE", Color.fromRGB(NamedTextColor.BLUE.value())}
        };
    }

    @Test(dataProvider = "getColorData")
    public void getColorTest(String input, Color expected) {
        assertEquals(
                Utils.getColor(input),
                expected
        );
    }

    @DataProvider
    public Object[][] literalSplitData() {
        return new Object[][] {
                {"simple;split", List.of("simple", "split")},
                {";empty values;", List.of("", "empty values", "")},
                {"", List.of("")},
                {";", List.of("", "")}
        };
    }

    @Test(dataProvider = "literalSplitData")
    public void literalSplitTest(String input, List<String> expected) {
        assertEquals(
                Utils.literalSplit(input, ";"),
                expected
        );
    }
}