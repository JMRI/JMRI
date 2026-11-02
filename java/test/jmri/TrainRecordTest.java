package jmri;

import java.util.List;

import jmri.TrainRecord.Detection;
import jmri.TrainRecord.DetectionMarker;
import jmri.TrainRecord.DetectionMarker.Placement;
import jmri.TrainRecord.Unit;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the value classes in {@link TrainRecord}: units and detection
 * markers.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainRecordTest {

    @Test
    public void testUnit() {
        Unit u = new Unit("loco", 210.0);
        assertEquals("loco", u.getName());
        assertEquals(210.0, u.getLength());
        Unit none = new Unit(null, null);
        assertNull(none.getName());
        assertNull(none.getLength());
        assertEquals(new Unit("loco", 210.0), u);
        assertEquals(new Unit("loco", 210.0).hashCode(), u.hashCode());
        assertNotEquals(new Unit("loco", 200.0), u);
        assertNotEquals(new Unit("coach", 210.0), u);
    }

    @Test
    public void testUnitLengthMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new Unit("u", 0.0));
        assertThrows(IllegalArgumentException.class, () -> new Unit("u", -1.0));
        assertThrows(IllegalArgumentException.class, () -> new Unit("u", Double.NaN));
    }

    @Test
    public void testMarkerPlacements() {
        DetectionMarker w = DetectionMarker.wholeTrain();
        assertEquals(Placement.WHOLE_TRAIN, w.getPlacement());
        assertNull(w.getOffset());
        assertNull(w.getLength());

        DetectionMarker f = DetectionMarker.front(120.0);
        assertEquals(Placement.FRONT, f.getPlacement());
        assertNull(f.getOffset());
        assertEquals(120.0, f.getLength());

        DetectionMarker r = DetectionMarker.rear(null);
        assertEquals(Placement.REAR, r.getPlacement());
        assertNull(r.getLength());

        DetectionMarker o = DetectionMarker.atOffset(300.0, 150.0);
        assertEquals(Placement.OFFSET, o.getPlacement());
        assertEquals(300.0, o.getOffset());
        assertEquals(150.0, o.getLength());

        DetectionMarker unknown = DetectionMarker.atOffset(null, 150.0);
        assertNull(unknown.getOffset(), "an unknown offset is kept as unknown");

        assertEquals(0.0, DetectionMarker.atOffset(0.0, null).getOffset(), "an offset of 0 is allowed");
    }

    @Test
    public void testMarkerChecks() {
        assertThrows(IllegalArgumentException.class, () -> DetectionMarker.front(0.0));
        assertThrows(IllegalArgumentException.class, () -> DetectionMarker.rear(-5.0));
        assertThrows(IllegalArgumentException.class, () -> DetectionMarker.atOffset(-1.0, 100.0));
        assertThrows(IllegalArgumentException.class, () -> DetectionMarker.atOffset(Double.NaN, 100.0));
    }

    @Test
    public void testMarkerEquality() {
        assertEquals(DetectionMarker.atOffset(300.0, 150.0), DetectionMarker.atOffset(300.0, 150.0));
        assertEquals(DetectionMarker.atOffset(300.0, 150.0).hashCode(), DetectionMarker.atOffset(300.0, 150.0).hashCode());
        assertNotEquals(DetectionMarker.atOffset(300.0, 150.0), DetectionMarker.atOffset(310.0, 150.0));
        assertNotEquals(DetectionMarker.front(100.0), DetectionMarker.rear(100.0));
        assertEquals(DetectionMarker.wholeTrain(), DetectionMarker.wholeTrain());
    }

    @Test
    public void testStandardMarkers() {
        assertEquals(List.of(DetectionMarker.wholeTrain()), DetectionMarker.forDetection(Detection.ENTIRE_TRAIN));
        assertEquals(List.of(DetectionMarker.front(null)), DetectionMarker.forDetection(Detection.HEAD_ONLY));
        assertEquals(List.of(DetectionMarker.front(null), DetectionMarker.rear(null)),
                DetectionMarker.forDetection(Detection.HEAD_AND_TAIL));
        assertThrows(IllegalArgumentException.class, () -> DetectionMarker.forDetection(Detection.CUSTOM));
        List<DetectionMarker> l = DetectionMarker.forDetection(Detection.HEAD_ONLY);
        assertThrows(UnsupportedOperationException.class, () -> l.add(DetectionMarker.rear(null)));
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
