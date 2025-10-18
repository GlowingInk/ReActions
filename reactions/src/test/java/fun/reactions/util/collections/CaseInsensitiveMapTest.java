package fun.reactions.util.collections;

import org.testng.annotations.Test;

import java.util.Map;

import static fun.reactions.util.collections.CollectionUtils.caseInsensitiveMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class CaseInsensitiveMapTest {
    @Test
    public void test() {
        Map<String, String> map = caseInsensitiveMap();
        map.put("One", "One");
        map.put("Two", "Two");
        map.put("onE", "Three");
        map.put("Four", "Four");
        assertEquals(
                map.get("One"),
                "Three"
        );
        assertEquals(
                map.get("two"),
                "Two"
        );
        assertNotEquals(
                Map.of(
                        "One", "One",
                        "Two", "Two",
                        "Four", "Four"
                ),
                map
        );
        assertNotEquals(
                map,
                Map.of(
                        "One", "One",
                        "Two", "Two",
                        "Four", "Four"
                )
        );
        assertEquals(
                Map.of(
                        "Two", "Two",
                        "onE", "Three",
                        "Four", "Four"
                ),
                map
        );
        assertEquals(
                map,
                Map.of(
                        "Two", "Two",
                        "onE", "Three",
                        "Four", "Four"
                )
        );
    }
}
