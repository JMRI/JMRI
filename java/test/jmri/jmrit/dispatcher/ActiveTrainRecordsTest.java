package jmri.jmrit.dispatcher;

import java.util.List;
import java.util.Objects;

import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.TrainRecord.Detection;
import jmri.TrainRecord.DetectionMarker;
import jmri.TrainRecord.LengthSource;
import jmri.TrainRecordManager;
import jmri.implementation.DefaultTransit;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ActiveTrainRecords} and the record an {@link ActiveTrain}
 * refers to.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ActiveTrainRecordsTest {

    private TrainRecordManager manager;

    private static ActiveTrain userTrain(String name) {
        return new ActiveTrain(new DefaultTransit("TT1"), name, ActiveTrain.USER);
    }

    private static ActiveTrain rosterTrain(String name, RosterEntry entry) {
        ActiveTrain at = new ActiveTrain(new DefaultTransit("TT1"), name, ActiveTrain.ROSTER);
        at.setRosterEntry(entry);
        return at;
    }

    private static RosterEntry entry(String id) {
        RosterEntry e = new RosterEntry();
        e.setId(id);
        return e;
    }

    @Test
    public void testTrainWithoutRosterEntryByName() {
        ActiveTrain first = userTrain("1A23");
        TrainRecord r = ActiveTrainRecords.link(first);
        assertSame(r, first.getTrainRecord());
        assertEquals("1A23", r.getName());
        assertNull(r.getRosterEntry());
        ActiveTrain again = userTrain("1A23");
        assertSame(r, ActiveTrainRecords.link(again), "the same train, run again, has the same record");
        assertEquals(1, manager.getRecords().size());
        assertNotSame(r, ActiveTrainRecords.link(userTrain("2B45")), "another train has its own");
    }

    @Test
    public void testTrainWithRosterEntryByEntry() {
        RosterEntry e = entry("Class 43");
        TrainRecord r = ActiveTrainRecords.link(rosterTrain("Class 43", e));
        assertSame(e, r.getRosterEntry());
        assertEquals("Class 43", r.getName());
        assertSame(r, ActiveTrainRecords.link(rosterTrain("HST up", e)),
                "the loco's record, whatever this run is called");
    }

    @Test
    public void testTrainWithSecondLocoByEntry() {
        RosterEntry lead = entry("Class 20 A");
        RosterEntry second = entry("Class 20 B");
        TrainRecord r = manager.newRecord("Pair of 20s");
        r.setRosterEntries(List.of(lead, second));
        assertSame(r, ActiveTrainRecords.link(rosterTrain("Coal", second)),
                "the record that has the loco among its locos");
        assertEquals(List.of(lead, second), r.getRosterEntries(), "the locos and their order left alone");
        assertEquals(1, manager.getRecords().size());
    }

    @Test
    public void testRecordByNameGetsTheRosterEntry() {
        TrainRecord r = ActiveTrainRecords.link(userTrain("1A23"));
        RosterEntry e = entry("Class 43");
        assertSame(r, ActiveTrainRecords.link(rosterTrain("1A23", e)), "a named record with no loco takes this one");
        assertSame(e, r.getRosterEntry());
    }

    @Test
    public void testNameTakenByAnotherLoco() {
        TrainRecord ra = ActiveTrainRecords.link(rosterTrain("Express", entry("Class 43")));
        assertEquals("Express", ra.getName());
        TrainRecord rb = ActiveTrainRecords.link(rosterTrain("Express", entry("Class 50")));
        assertNotSame(ra, rb, "another loco is another train");
        assertEquals("Class 50", rb.getName(), "named after its roster entry");
        assertEquals("Class 50", Objects.requireNonNull(rb.getRosterEntry(), "roster entry").getId());

        manager.newRecord("Class 31");
        TrainRecord rc = ActiveTrainRecords.link(rosterTrain("Express", entry("Class 31")));
        assertEquals("Express Class 31", rc.getName(), "then after both");

        manager.newRecord("Class 37");
        manager.newRecord("Express Class 37");
        TrainRecord rd = ActiveTrainRecords.link(rosterTrain("Express", entry("Class 37")));
        assertEquals("Express (2)", rd.getName(), "numbered once every other name is taken");
        assertEquals("Class 37", Objects.requireNonNull(rd.getRosterEntry(), "roster entry").getId());
    }

    @Test
    public void testDetectionAndLengthRecorded() {
        ActiveTrain at = userTrain("1A23");
        at.setTrainDetection(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        at.setMaxTrainLengthMM(1200);
        TrainRecord r = ActiveTrainRecords.link(at);
        assertEquals(Detection.HEAD_AND_TAIL, r.getDetection());
        assertEquals(Double.valueOf(1200.0), r.getLength(LengthSource.ENTERED));

        at.setTrainDetection(TrainDetection.TRAINDETECTION_WHOLETRAIN);
        at.setMaxTrainLengthMM(0);
        assertEquals(Detection.ENTIRE_TRAIN, r.getDetection(), "changes are recorded too");
        assertNull(r.getLength(LengthSource.ENTERED), "no Max Train Length, no entered length");
    }

    @Test
    public void testCustomMarkersKept() {
        TrainRecord r = manager.newRecord("1A23");
        List<DetectionMarker> markers = List.of(DetectionMarker.atOffset(300.0, 250.0));
        r.setCustomMarkers(markers);
        ActiveTrain at = userTrain("1A23");
        at.setTrainDetection(TrainDetection.TRAINDETECTION_HEADONLY);
        ActiveTrainRecords.link(at);
        assertEquals(markers, r.getMarkers(), "a custom list is kept: Train Detection can't express it");
    }

    @Test
    public void testRemovedRecordNotChanged() {
        ActiveTrain at = userTrain("1A23");
        TrainRecord r = ActiveTrainRecords.link(at);
        manager.remove(r);
        at.setMaxTrainLengthMM(900);
        assertNull(r.getLength(LengthSource.ENTERED), "a removed record is left alone");
        assertNotSame(r, ActiveTrainRecords.link(userTrain("1A23")), "and a new one is made next time");
    }

    @Test
    public void testNoRecordUntilLinked() {
        ActiveTrain at = userTrain("1A23");
        at.setMaxTrainLengthMM(900);
        assertNull(at.getTrainRecord());
        assertTrue(manager.getRecords().isEmpty());
    }

    @Test
    public void testToRecordDetection() {
        assertEquals(Detection.ENTIRE_TRAIN, ActiveTrain.toRecordDetection(TrainDetection.TRAINDETECTION_WHOLETRAIN));
        assertEquals(Detection.HEAD_ONLY, ActiveTrain.toRecordDetection(TrainDetection.TRAINDETECTION_HEADONLY));
        assertEquals(Detection.HEAD_AND_TAIL, ActiveTrain.toRecordDetection(TrainDetection.TRAINDETECTION_HEADANDTAIL));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        manager = InstanceManager.getDefault(TrainRecordManager.class);
    }

    @AfterEach
    public void tearDown() {
        manager = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
