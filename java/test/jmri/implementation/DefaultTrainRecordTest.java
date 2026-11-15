package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.BasicRosterEntry;
import jmri.IdTag;
import jmri.TrainRecord;
import jmri.TrainRecord.Detection;
import jmri.TrainRecord.DetectionMarker;
import jmri.TrainRecord.LengthSource;
import jmri.TrainRecord.TrainComponent;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DefaultTrainRecord} on its own, with a registry that
 * refuses what it is told to.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultTrainRecordTest {

    private TestRegistry registry;
    private DefaultTrainRecord record;
    private List<PropertyChangeEvent> events;

    @Test
    public void testNewRecord() {
        assertEquals("1A23", record.getName());
        assertEquals("1A23", record.toString());
        assertNull(record.getFirstRosterEntry());
        assertTrue(record.getIdTags().isEmpty());
        assertEquals(List.of(new TrainComponent(null, null)), record.getTrainComponents(), "one component with no length");
        assertEquals(Detection.ENTIRE_TRAIN, record.getDetection());
        assertEquals(List.of(DetectionMarker.wholeTrain()), record.getMarkers());
        for (LengthSource s : LengthSource.values()) {
            assertNull(record.getLength(s), s.toString());
        }
        assertNull(record.getHoldingLength());
        assertNull(record.getPlacingLength());
        assertFalse(record.isMeasuredLengthConsistent());
        assertFalse(record.isDisposed());
    }

    @Test
    public void testNormalizeName() {
        assertEquals("1A23", DefaultTrainRecord.normalizeName("  1A23 "));
        assertThrows(IllegalArgumentException.class, () -> DefaultTrainRecord.normalizeName(""));
        assertThrows(IllegalArgumentException.class, () -> DefaultTrainRecord.normalizeName("   "));
    }

    @Test
    public void testSetName() {
        record.setName(" 2B45 ");
        assertEquals("2B45", record.getName());
        assertEquals(List.of("2B45"), registry.names, "the registry was asked first");
        assertEquals(1, events.size());
        assertEquals(TrainRecord.PROPERTY_NAME, events.get(0).getPropertyName());
        assertEquals("1A23", events.get(0).getOldValue());
        assertEquals("2B45", events.get(0).getNewValue());

        record.setName("2B45");
        assertEquals(1, events.size(), "the same name changes nothing");
        assertEquals(1, registry.names.size(), "and isn't checked again");

        assertThrows(IllegalArgumentException.class, () -> record.setName(" "));
        registry.refuse = true;
        assertThrows(IllegalArgumentException.class, () -> record.setName("3C67"));
        assertEquals("2B45", record.getName(), "a refused name isn't taken");
        assertEquals(1, events.size());
    }

    @Test
    public void testRosterEntry() {
        RosterEntry e = new RosterEntry();
        e.setId("Class 43");
        record.insertFirstRosterEntry(e);
        assertSame(e, record.getFirstRosterEntry());
        assertEquals(List.of(e), record.getRosterEntries());
        assertEquals(1, registry.entries.size());
        assertEquals(List.of(TrainRecord.PROPERTY_ROSTER_ENTRIES), propertyNames());

        assertThrows(IllegalArgumentException.class, () -> record.insertFirstRosterEntry(e),
                "an entry the record already has isn't added again");
        assertEquals(List.of(e), record.getRosterEntries());
        assertEquals(1, events.size(), "a refused entry changes nothing");

        record.removeFirstRosterEntry();
        assertNull(record.getFirstRosterEntry());
        assertTrue(record.getRosterEntries().isEmpty());
        assertEquals(2, events.size());
        assertEquals(1, registry.entries.size(), "removing isn't checked");
        record.removeFirstRosterEntry();
        assertEquals(2, events.size(), "removing from none changes nothing");

        registry.refuse = true;
        assertThrows(IllegalArgumentException.class, () -> record.insertFirstRosterEntry(e));
        assertNull(record.getFirstRosterEntry(), "a refused entry isn't taken");
    }

    @Test
    public void testRosterEntriesOfSeveralLocos() {
        RosterEntry lead = new RosterEntry();
        lead.setId("47012");
        RosterEntry second = new RosterEntry();
        second.setId("47013");
        record.setRosterEntries(List.of(lead, second));
        assertEquals(List.of(lead, second), record.getRosterEntries());
        assertSame(lead, record.getFirstRosterEntry(), "the first is the lead loco");
        assertEquals(List.of(lead, second), registry.entries, "each new entry is checked");
        assertEquals(List.of(TrainRecord.PROPERTY_ROSTER_ENTRIES), propertyNames());

        events.clear();
        record.setRosterEntries(List.of(second, lead));
        assertEquals(List.of(second, lead), record.getRosterEntries(), "reordered as a whole list");
        assertEquals(2, registry.entries.size(), "entries the record already has aren't checked again");
        assertEquals(List.of(TrainRecord.PROPERTY_ROSTER_ENTRIES), propertyNames());

        record.removeFirstRosterEntry();
        assertEquals(List.of(lead), record.getRosterEntries(), "the next loco leads");

        RosterEntry banker = new RosterEntry();
        banker.setId("37425");
        record.insertFirstRosterEntry(banker);
        assertEquals(List.of(banker, lead), record.getRosterEntries(), "added at the front");
        assertEquals(List.of(lead, second, banker), registry.entries, "the new entry is checked");

        record.removeFirstRosterEntry();
        record.removeFirstRosterEntry();
        assertTrue(record.getRosterEntries().isEmpty(), "removing each in turn");

        RosterEntry sameId = new RosterEntry();
        sameId.setId("47012");
        assertThrows(IllegalArgumentException.class, () -> record.setRosterEntries(List.of(lead, sameId)),
                "the same loco can't be listed twice");
        assertTrue(record.getRosterEntries().isEmpty(), "a refused list isn't taken");

        record.getRosterEntries().add(lead);
        assertTrue(record.getRosterEntries().isEmpty(), "the list returned is a copy");
    }

    @Test
    public void testIdTags() {
        IdTag a = new DefaultIdTag("ID1");
        IdTag b = new DefaultIdTag("ID2");
        record.addIdTag(a);
        record.addIdTag(b);
        assertEquals(List.of(a, b), new ArrayList<>(record.getIdTags()), "in the order added");
        assertEquals(2, events.size());
        record.addIdTag(a);
        assertEquals(2, events.size(), "adding a tag again changes nothing");

        record.getIdTags().clear();
        assertEquals(2, record.getIdTags().size(), "the set returned is a copy");

        record.removeIdTag(a);
        assertEquals(List.of(b), new ArrayList<>(record.getIdTags()));
        assertEquals(3, events.size());
        record.removeIdTag(a);
        assertEquals(3, events.size(), "removing a tag it hasn't got changes nothing");

        registry.refuse = true;
        assertThrows(IllegalArgumentException.class, () -> record.addIdTag(a));
        assertFalse(record.getIdTags().contains(a), "a refused tag isn't taken");
    }

    @Test
    public void testTrainComponents() {
        List<TrainComponent> components = List.of(new TrainComponent("loco", 220.0), new TrainComponent("coaches", 800.0));
        record.setTrainComponents(components);
        assertEquals(components, record.getTrainComponents());
        assertEquals(Double.valueOf(1020.0), record.getLength(LengthSource.TRAIN_COMPONENTS), "the sum of the components' lengths");
        assertEquals(List.of(TrainRecord.PROPERTY_TRAIN_COMPONENTS, TrainRecord.PROPERTY_LENGTHS), propertyNames());

        record.setTrainComponents(new ArrayList<>(components));
        assertEquals(2, events.size(), "the same components change nothing");

        record.setTrainComponents(List.of(new TrainComponent("loco", 220.0), new TrainComponent("wagons", null)));
        assertNull(record.getLength(LengthSource.TRAIN_COMPONENTS), "no length unless every component has one");
        assertEquals(4, events.size());

        record.setTrainComponents(List.of(new TrainComponent("loco", 220.0), new TrainComponent("vans", null)));
        assertEquals(5, events.size(), "components changed but their length did not");
        assertEquals(TrainRecord.PROPERTY_TRAIN_COMPONENTS, events.get(4).getPropertyName());

        assertThrows(IllegalArgumentException.class, () -> record.setTrainComponents(List.of()));
        record.getTrainComponents().clear();
        assertEquals(2, record.getTrainComponents().size(), "the list returned is a copy");
    }

    @Test
    public void testStandardDetection() {
        record.setDetection(Detection.HEAD_ONLY);
        assertEquals(Detection.HEAD_ONLY, record.getDetection());
        assertEquals(List.of(DetectionMarker.front(null)), record.getMarkers());
        record.setDetection(Detection.HEAD_AND_TAIL);
        assertEquals(List.of(DetectionMarker.front(null), DetectionMarker.rear(null)), record.getMarkers());
        assertEquals(2, events.size());
        record.setDetection(Detection.HEAD_AND_TAIL);
        assertEquals(2, events.size(), "the same setting changes nothing");
        assertThrows(IllegalArgumentException.class, () -> record.setDetection(Detection.CUSTOM));
    }

    @Test
    public void testCustomMarkers() {
        List<DetectionMarker> markers = List.of(DetectionMarker.front(230.0), DetectionMarker.atOffset(null, 250.0));
        record.setCustomMarkers(markers);
        assertEquals(Detection.CUSTOM, record.getDetection());
        assertEquals(markers, record.getMarkers());
        assertEquals(1, events.size());
        assertEquals(TrainRecord.PROPERTY_DETECTION, events.get(0).getPropertyName());
        assertEquals(List.of(DetectionMarker.wholeTrain()), events.get(0).getOldValue(), "the old markers");
        assertEquals(markers, events.get(0).getNewValue(), "the new markers");

        record.setCustomMarkers(new ArrayList<>(markers));
        assertEquals(1, events.size(), "the same list changes nothing");

        record.setCustomMarkers(List.of(DetectionMarker.atOffset(400.0, 250.0)));
        assertEquals(2, events.size(), "a different list is a change, though detection stays custom");

        record.setDetection(Detection.HEAD_ONLY);
        assertEquals(List.of(DetectionMarker.front(null)), record.getMarkers(), "a standard setting forgets the list");

        assertThrows(IllegalArgumentException.class, () -> record.setCustomMarkers(List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> record.setCustomMarkers(List.of(DetectionMarker.wholeTrain())));
        assertEquals(Detection.HEAD_ONLY, record.getDetection(), "a refused list changes nothing");
    }

    @Test
    public void testHoldingLengthIsTheLongest() {
        record.setLength(LengthSource.ENTERED, 900.0);
        record.setLength(LengthSource.ROSTER, 250.0);
        assertEquals(900.0, record.getHoldingLength());
        record.setTrainComponents(List.of(new TrainComponent("loco", 250.0), new TrainComponent("coaches", 700.0)));
        assertEquals(Double.valueOf(950.0), record.getHoldingLength(), "the components' length counts");
        record.setMeasuredLength(1010.0, false);
        assertEquals(1010.0, record.getHoldingLength(), "a measured length counts, consistent or not");
        record.setLength(LengthSource.OPERATIONS, 1200.0);
        assertEquals(1200.0, record.getHoldingLength());
        record.setLength(LengthSource.OPERATIONS, null);
        assertNull(record.getLength(LengthSource.OPERATIONS));
        assertEquals(1010.0, record.getHoldingLength());
    }

    @Test
    public void testPlacingLength() {
        record.setLength(LengthSource.ENTERED, 900.0);
        assertEquals(900.0, record.getPlacingLength(), "the holding length until a measurement is consistent");
        record.setMeasuredLength(850.0, false);
        assertFalse(record.isMeasuredLengthConsistent());
        assertEquals(900.0, record.getPlacingLength());
        record.setMeasuredLength(850.0, true);
        assertTrue(record.isMeasuredLengthConsistent());
        assertEquals(850.0, record.getPlacingLength(), "the consistent measured length, though shorter");
        assertEquals(900.0, record.getHoldingLength(), "holding still uses the longest");
        record.setMeasuredLength(null, true);
        assertFalse(record.isMeasuredLengthConsistent(), "no measured length can't be consistent");
        assertEquals(900.0, record.getPlacingLength());
    }

    @Test
    public void testLengthEvents() {
        record.setLength(LengthSource.ENTERED, 900.0);
        record.setLength(LengthSource.ENTERED, 900.0);
        record.setMeasuredLength(850.0, false);
        record.setMeasuredLength(850.0, false);
        record.setMeasuredLength(850.0, true);
        assertEquals(List.of(TrainRecord.PROPERTY_LENGTHS, TrainRecord.PROPERTY_LENGTHS, TrainRecord.PROPERTY_LENGTHS),
                propertyNames(), "a change only when a length or consistency changes");
    }

    @Test
    public void testLengthChecks() {
        assertThrows(IllegalArgumentException.class, () -> record.setLength(LengthSource.TRAIN_COMPONENTS, 100.0));
        assertThrows(IllegalArgumentException.class, () -> record.setLength(LengthSource.MEASURED, 100.0));
        assertThrows(IllegalArgumentException.class, () -> record.setLength(LengthSource.ENTERED, 0.0));
        assertThrows(IllegalArgumentException.class, () -> record.setLength(LengthSource.ROSTER, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> record.setMeasuredLength(-1.0, true));
        assertTrue(events.isEmpty());
    }

    @Test
    public void testPlacing() {
        jmri.Block a = new jmri.Block("IBA", "A");
        jmri.Block b = new jmri.Block("IBB", "B");
        record.place(List.of(a, b), true);
        assertTrue(record.isPlaced());
        assertEquals(List.of(a, b), record.getBlocks());
        assertTrue(record.isFrontKnown());
        assertNull(record.getLeadingEnd(), "not seen to move");
        assertEquals(TrainRecord.Confidence.CONFIRMED, record.getConfidence());
        assertEquals(2, record.getTrail().size(), "the trail starts from where it was placed");
        assertEquals(1, registry.placements, "the registry is told");
        assertThrows(IllegalArgumentException.class, () -> record.place(List.of(), true));
        assertThrows(IllegalArgumentException.class, () -> record.place(List.of(a, a), true));
        record.unplace();
        assertFalse(record.isPlaced());
        assertNull(record.getConfidence());
        assertFalse(record.isFrontKnown());
        assertEquals(2, registry.placements);
        record.unplace();
        assertEquals(2, registry.placements, "unplacing an unplaced record does nothing");
    }

    @Test
    public void testRemovedRecordCantBeChanged() {
        record.dispose();
        assertTrue(record.isDisposed());
        IdTag tag = new DefaultIdTag("ID1");
        assertThrows(IllegalStateException.class, () -> record.setName("X"));
        RosterEntry e = new RosterEntry();
        e.setId("Class 43");
        assertThrows(IllegalStateException.class, () -> record.insertFirstRosterEntry(e));
        assertThrows(IllegalStateException.class, () -> record.removeFirstRosterEntry());
        assertThrows(IllegalStateException.class, () -> record.addIdTag(tag));
        assertThrows(IllegalStateException.class, () -> record.removeIdTag(tag));
        assertThrows(IllegalStateException.class, () -> record.setTrainComponents(List.of(new TrainComponent(null, null))));
        assertThrows(IllegalStateException.class, () -> record.setDetection(Detection.HEAD_ONLY));
        assertThrows(IllegalStateException.class,
                () -> record.setCustomMarkers(List.of(DetectionMarker.front(null))));
        assertThrows(IllegalStateException.class, () -> record.setLength(LengthSource.ENTERED, 1.0));
        assertThrows(IllegalStateException.class, () -> record.setMeasuredLength(1.0, true));
        assertThrows(IllegalStateException.class, () -> record.place(List.of(new jmri.Block("IBA", "A")), true));
        assertThrows(IllegalStateException.class, () -> record.unplace());
        assertEquals("1A23", record.getName(), "it can still be read");
        assertTrue(events.isEmpty());
    }

    private List<String> propertyNames() {
        List<String> names = new ArrayList<>();
        events.forEach(e -> names.add(e.getPropertyName()));
        return names;
    }

    /**
     * Records what it is asked, and refuses everything once told to.
     */
    private static class TestRegistry implements DefaultTrainRecord.Registry {

        boolean refuse = false;
        final List<String> names = new ArrayList<>();
        final List<BasicRosterEntry> entries = new ArrayList<>();
        final List<IdTag> tags = new ArrayList<>();
        int placements = 0;

        @Override
        public void checkName(@Nonnull TrainRecord record, @Nonnull String name) {
            check();
            names.add(name);
        }

        @Override
        public void checkRosterEntry(@Nonnull TrainRecord record, @Nonnull BasicRosterEntry entry) {
            check();
            entries.add(entry);
        }

        @Override
        public void checkIdTag(@Nonnull TrainRecord record, @Nonnull IdTag tag) {
            check();
            tags.add(tag);
        }

        @Override
        public void placementChanged(@Nonnull TrainRecord record) {
            placements++;
        }

        private void check() {
            if (refuse) {
                throw new IllegalArgumentException("refused");
            }
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        registry = new TestRegistry();
        record = new DefaultTrainRecord("1A23", registry);
        events = new ArrayList<>();
        record.addPropertyChangeListener(events::add);
    }

    @AfterEach
    public void tearDown() {
        record = null;
        registry = null;
        events = null;
        JUnitUtil.tearDown();
    }
}
