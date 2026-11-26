package fun.reactions.util.time;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fun.reactions.util.time.TimeUtils.*;
import static org.testng.Assert.assertEquals;

public class TimeUtilsTest {
    @DataProvider
    public Object[][] parseTimeSplittedData() {
        return new Object[][] {
                {"11:22:33.44", 11*MS_PER_HOUR + 22*MS_PER_MINUTE + (long) (33.44*MS_PER_SECOND)},
                {"11:22", 11*MS_PER_HOUR + 22*MS_PER_MINUTE}
        };
    }

    @Test(dataProvider = "parseTimeSplittedData")
    public void parseTimeSplittedTest(String timeStr, long expectedTime) {
        assertEquals(
                parseTimeSplitted(timeStr),
                expectedTime
        );
    }

    @DataProvider
    public Object[][] parseTimePreciseData() {
        return new Object[][] {
                {"1d 2h 3m 4s 5t 6ms", MS_PER_DAY + 2*MS_PER_HOUR + 3*MS_PER_MINUTE + 4*MS_PER_SECOND + 5*MS_PER_TICK + 6},
                {"1d2h3", MS_PER_DAY + 2*MS_PER_HOUR + 3*MS_PER_SECOND}
        };
    }

    @Test(dataProvider = "parseTimePreciseData")
    public void parseTimePreciseTest(String timeStr, long expectedTime) {
        assertEquals(
                parseTimePrecise(timeStr),
                expectedTime
        );
    }

    @DataProvider
    public Object[][] parseTimeData() {
        List<Object[]> data = new ArrayList<>();
        data.addAll(Arrays.asList(parseTimeSplittedData()));
        data.addAll(Arrays.asList(parseTimePreciseData()));
        return data.toArray(new Object[][]{});
    }

    @Test(dataProvider = "parseTimeData")
    public void parseTimeTest(String timeStr, long expectedTime) {
        assertEquals(
                parseTime(timeStr),
                expectedTime
        );
    }

    @DataProvider
    public Object[][] formatDurationData() {
        return new Object[][] {
                {0L, "0s"},
                {999L, "0s"},
                {45 * MS_PER_SECOND, "45s"},
                {MS_PER_MINUTE, "1m"},
                {MS_PER_HOUR + 5 * MS_PER_SECOND, "1h 0m 5s"},
                {2 * MS_PER_DAY + 3 * MS_PER_HOUR + 4 * MS_PER_MINUTE + 5 * MS_PER_SECOND, "2d 3h 4m 5s"}
        };
    }

    @Test(dataProvider = "formatDurationData")
    public void formatDurationTest(long msTime, String expected) {
        assertEquals(
                formatDuration(msTime),
                expected
        );
    }

    @DataProvider
    public Object[][] formatIngameTimeData() {
        return new Object[][] {
                {0L, "06:00"},
                {984L, "06:59"},
                {6000L, "12:00"},
                {18000L, "00:00"},
                {23999L, "05:59"}
        };
    }

    @Test(dataProvider = "formatIngameTimeData")
    public void formatIngameTimeTest(long ticks, String expected) {
        assertEquals(
                formatIngameTime(ticks, false),
                expected
        );
    }
}
