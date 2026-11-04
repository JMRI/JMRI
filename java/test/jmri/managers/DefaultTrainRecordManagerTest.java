package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import jmri.IdTag;
import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.implementation.DefaultIdTag;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DefaultTrainRecordManager}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultTrainRecordManagerTest {

    private TrainRecordManager manager;
    private List<PropertyChangeEvent> events;

    @Test
    public void testDefaultFromInstanceManager() {
        TrainRecordManager m = InstanceManager.getDefault(TrainRecordManager.class);
        assertTrue(m instanceof DefaultTrainRecordManager);
        assertSame(m, InstanceManager.getDefault(TrainRecordManager.class), "one default");
    }

    @Test
    public void testNewRecord() {
        TrainRecord r = manager.newRecord(" 1A23 ");
        assertEquals("1A23", r.getName(), "the name is normalised");
        assertEquals(List.of(r), manager.getRecords());
        assertEquals(1, events.size());
        assertEquals(TrainRecordManager.PROPERTY_RECORDS, events.get(0).getPropertyName());
        assertNull(events.get(0).getOldValue());
        assertSame(r, events.get(0).getNewValue());
        assertSame(r, manager.getByName("1A23"));
        assertSame(r, manager.getByName(" 1A23"), "found whatever the spaces");
        assertNull(manager.getByName("2B45"));
    }

    @Test
    public void testNamesAreUnique() {
        manager.newRecord("1A23");
        TrainRecord b = manager.newRecord("2B45");
        assertThrows(IllegalArgumentException.class, () -> manager.newRecord("1A23"));
        assertThrows(IllegalArgumentException.class, () -> manager.newRecord("1A23 "));
        assertThrows(IllegalArgumentException.class, () -> manager.newRecord(""));
        assertThrows(IllegalArgumentException.class, () -> b.setName("1A23"));
        assertEquals("2B45", b.getName());
        assertEquals(2, manager.getRecords().size());

        b.setName("3C67");
        assertSame(b, manager.getByName("3C67"), "found by its new name");
        assertNull(manager.getByName("2B45"));
        assertNotNull(manager.newRecord("2B45"), "the old name is free again");
    }

    @Test
    public void testRecordsInOrderCreated() {
        TrainRecord c = manager.newRecord("C");
        TrainRecord a = manager.newRecord("A");
        TrainRecord b = manager.newRecord("B");
        assertEquals(List.of(c, a, b), manager.getRecords());
        manager.getRecords().clear();
        assertEquals(3, manager.getRecords().size(), "the list returned is a copy");
    }

    @Test
    public void testRosterEntryBelongsToOneRecord() {
        RosterEntry e = new RosterEntry();
        e.setId("Class 43");
        TrainRecord a = manager.newRecord("A");
        TrainRecord b = manager.newRecord("B");
        assertNull(manager.getByRosterEntry(e));
        a.setRosterEntry(e);
        assertSame(a, manager.getByRosterEntry(e));
        assertThrows(IllegalArgumentException.class, () -> b.setRosterEntry(e));
        assertNull(b.getRosterEntry());

        RosterEntry sameId = new RosterEntry();
        sameId.setId("Class 43");
        assertSame(a, manager.getByRosterEntry(sameId), "an entry with the same ID is the same loco");
        assertThrows(IllegalArgumentException.class, () -> b.setRosterEntry(sameId));

        a.setRosterEntry(null);
        b.setRosterEntry(e);
        assertSame(b, manager.getByRosterEntry(e), "free once the other record lets it go");
    }

    @Test
    public void testSecondLocoBelongsToOneRecord() {
        RosterEntry lead = new RosterEntry();
        lead.setId("47012");
        RosterEntry second = new RosterEntry();
        second.setId("47013");
        TrainRecord a = manager.newRecord("A");
        TrainRecord b = manager.newRecord("B");
        a.setRosterEntries(java.util.List.of(lead, second));
        assertSame(a, manager.getByRosterEntry(second), "found by a loco that isn't the lead");
        assertThrows(IllegalArgumentException.class, () -> b.setRosterEntry(second));
        assertThrows(IllegalArgumentException.class, () -> b.setRosterEntries(java.util.List.of(second)));
        assertTrue(b.getRosterEntries().isEmpty(), "a refused entry isn't taken");

        a.setRosterEntries(java.util.List.of(lead));
        b.setRosterEntry(second);
        assertSame(b, manager.getByRosterEntry(second), "free once the other record lets it go");
    }

    @Test
    public void testIdTagBelongsToOneRecord() {
        IdTag tag = new DefaultIdTag("ID1234");
        TrainRecord a = manager.newRecord("A");
        TrainRecord b = manager.newRecord("B");
        assertNull(manager.getByIdTag(tag));
        a.addIdTag(tag);
        assertSame(a, manager.getByIdTag(tag));
        assertThrows(IllegalArgumentException.class, () -> b.addIdTag(tag));
        assertTrue(b.getIdTags().isEmpty());
        a.removeIdTag(tag);
        b.addIdTag(tag);
        assertSame(b, manager.getByIdTag(tag));
    }

    @Test
    public void testRemove() {
        RosterEntry e = new RosterEntry();
        e.setId("Class 43");
        IdTag tag = new DefaultIdTag("ID1234");
        TrainRecord a = manager.newRecord("A");
        a.setRosterEntry(e);
        a.addIdTag(tag);
        events.clear();

        manager.remove(a);
        assertTrue(a.isRemoved());
        assertTrue(manager.getRecords().isEmpty());
        assertEquals(1, events.size());
        assertSame(a, events.get(0).getOldValue());
        assertNull(events.get(0).getNewValue());
        assertThrows(IllegalStateException.class, () -> a.setName("Z"));

        TrainRecord again = manager.newRecord("A");
        again.setRosterEntry(e);
        again.addIdTag(tag);
        assertSame(again, manager.getByRosterEntry(e), "a removed record's name, entry and tags are free");

        int before = events.size();
        manager.remove(a);
        assertEquals(before, events.size(), "removing a removed record does nothing");
        assertEquals(List.of(again), manager.getRecords());
    }

    @Test
    public void testRemoveAnotherManagersRecord() {
        TrainRecord other = new DefaultTrainRecordManager().newRecord("A");
        assertThrows(IllegalArgumentException.class, () -> manager.remove(other));
        assertFalse(other.isRemoved());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        manager = new DefaultTrainRecordManager();
        events = new ArrayList<>();
        manager.addPropertyChangeListener(events::add);
    }

    @AfterEach
    public void tearDown() {
        manager = null;
        events = null;
        JUnitUtil.tearDown();
    }
}
