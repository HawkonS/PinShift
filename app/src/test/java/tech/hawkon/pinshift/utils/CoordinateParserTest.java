package tech.hawkon.pinshift.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CoordinateParserTest {
    @Test
    public void acceptsSignedDecimalCoordinatesAndWhitespace() {
        assertEquals(-122.4194, CoordinateParser.parseLongitude(" -122.4194 "), 0);
        assertEquals(-33.8688, CoordinateParser.parseLatitude("-33.8688"), 0);
        assertEquals(0.5, CoordinateParser.parseLongitude("+.5"), 0);
        assertEquals(0.0, CoordinateParser.parseLatitude("0"), 0);
    }

    @Test
    public void acceptsInclusiveCoordinateBoundaries() {
        assertEquals(-180.0, CoordinateParser.parseLongitude("-180"), 0);
        assertEquals(180.0, CoordinateParser.parseLongitude("180"), 0);
        assertEquals(-90.0, CoordinateParser.parseLatitude("-90"), 0);
        assertEquals(90.0, CoordinateParser.parseLatitude("90"), 0);
    }

    @Test
    public void rejectsMalformedAndNonFiniteInputWithoutThrowing() {
        String[] invalid = {null, "", " ", "-", ".", "--1", "1-2", "1..2", "abc",
                "NaN", "Infinity", "-Infinity", "1e309", "-1e309"};
        for (String text : invalid) {
            assertNull(text, CoordinateParser.parseLongitude(text));
            assertNull(text, CoordinateParser.parseLatitude(text));
        }
    }

    @Test
    public void rejectsCoordinatesOutsideTheirRespectiveRanges() {
        assertNull(CoordinateParser.parseLongitude("-180.0001"));
        assertNull(CoordinateParser.parseLongitude("180.0001"));
        assertNull(CoordinateParser.parseLatitude("-90.0001"));
        assertNull(CoordinateParser.parseLatitude("90.0001"));
        assertNull(CoordinateParser.parseLatitude("120"));
        assertEquals(120.0, CoordinateParser.parseLongitude("120"), 0);
    }
}
