package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link Detection}: the detected stretches of a 200 mm train with a
 * 100 mm loco, worked by hand.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DetectionTest {

    @Test
    public void testModes() {
        assertEquals(Detection.Mode.ENTIRE_TRAIN, Detection.entireTrain().getMode());
        assertArrayEquals(new double[]{0, 200}, Detection.entireTrain().resolve(200, 100).get(0), 1e-9);
        assertArrayEquals(new double[]{0, 100}, Detection.headOnly().resolve(200, 100).get(0), 1e-9);
        List<double[]> ht = Detection.headAndTail(20).resolve(200, 100);
        assertEquals(2, ht.size());
        assertArrayEquals(new double[]{180, 20}, ht.get(1), 1e-9, "the tail marker at the rear");
        assertArrayEquals(new double[]{0, 150}, Detection.headOnly().resolve(150, 200).get(0), 1e-9,
                "a loco longer than the true train is cut to the train");
    }

    @Test
    public void testMarkers() {
        Detection.Marker m = new Detection.Marker(150, 100);
        assertEquals(150, m.getOffsetMm(), 1e-9);
        assertEquals(100, m.getLengthMm(), 1e-9);
        List<double[]> r = Detection.markers(List.of(m)).resolve(200, 100);
        assertArrayEquals(new double[]{150, 50}, r.get(0), 1e-9, "cut at the rear of the train");
        assertEquals(0, Detection.markers(List.of(new Detection.Marker(250, 10))).resolve(200, 100).size(),
                "a marker beyond the train is not there");
    }

    @Test
    public void testBadValues() {
        assertThrows(IllegalArgumentException.class, () -> new Detection.Marker(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> new Detection.Marker(0, 0));
        assertThrows(IllegalArgumentException.class, () -> Detection.headAndTail(0));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Detection.markers(List.of()));
        assertEquals("At least one detection marker is needed.", e.getMessage());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
