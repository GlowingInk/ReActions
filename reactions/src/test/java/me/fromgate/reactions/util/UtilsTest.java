package me.fromgate.reactions.util;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class UtilsTest {
    @DataProvider
    public Object[][] isStringEmptyData() {
        return new Object[][] {
                {"input", false},
                {" ", false},
                {null, true},
                {"", true}
        };
    }

    @Test(dataProvider = "isStringEmptyData")
    public void isStringEmptyTest(String input, boolean expected) {
        assertEquals(
                Utils.isStringEmpty(input),
                expected
        );
    }

    @DataProvider
    public Object[][] containsWordData() {
        return new Object[][] {
                {"some word", "abc,hello,some word,wow", true},
                {"no", "nowhere,nope,lol", false},
                {"Insensitive single", "insensitive single", true}
        };
    }

    @Test(dataProvider = "containsWordData")
    public void containsWordTest(String word, String text, boolean expected) {
        assertEquals(
                Utils.containsWord(word, text),
                expected
        );
    }

    @Test
    public void getFilledEmptyListTest() {
        String[] arr = new String[20];
        Arrays.fill(arr, "");
        assertEquals(
                Utils.getFilledEmptyList(20),
                List.of(arr)
        );
    }

    @DataProvider
    public Object[][] searchNotNullData() {
        return new Object[][] {
                {null, null, null, null},
                {"found", null, null, "found"},
                {"def", "def", null, null},
                {"not def", "def", null, "not def", "later in list"}
        };
    }

    @Test(dataProvider = "searchNotNullData")
    public void searchNotNullTest(String expected, String def, String... values) {
        assertEquals(
                Utils.searchNotNull(def, values),
                expected
        );
    }

    @DataProvider
    public Object[][] getEnumData() {
        return new Object[][] {
                {"UP", RoundingMode.DOWN, RoundingMode.UP},
                {"UNNECESSARY", null, RoundingMode.UNNECESSARY},
                {"unknown", null, null},
                {"unknown", RoundingMode.DOWN, RoundingMode.DOWN}
        };
    }

    @Test(dataProvider = "getEnumData")
    public void getEnumTest(String name, RoundingMode def, RoundingMode expected) {
        assertEquals(
                Utils.getEnum(RoundingMode.class, name, def),
                expected
        );
        if (def != null) {
            assertEquals(
                    Utils.getEnum(name, def),
                    expected
            );
        }
    }

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