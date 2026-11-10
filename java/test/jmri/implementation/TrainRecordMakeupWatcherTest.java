package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import jmri.InstanceManager;
import jmri.TrainMakeupSource;
import jmri.TrainMakeupSource.TrainMakeup;
import jmri.TrainRecord;
import jmri.TrainRecord.LengthSource;
import jmri.TrainRecord.TrainComponent;
import jmri.TrainRecordManager;
import jmri.beans.PropertyChangeSupport;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A train record takes its components and Operations length from what the
 * train of the same name is made up of.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainRecordMakeupWatcherTest {

    private TrainRecordManager manager;

    /** A source tests drive by hand. */
    private static final class Source extends PropertyChangeSupport implements TrainMakeupSource {

        final String train;
        TrainMakeup makeup;

        Source(String train, TrainMakeup makeup) {
            this.train = train;
            this.makeup = makeup;
        }

        @Override
        public TrainMakeup getMakeup(String trainName) {
            return train.equals(trainName) ? makeup : null;
        }

        /** Says the train's makeup changed, on another thread, as a source may. */
        void changed() {
            Thread t = new Thread(() -> firePropertyChange(PROPERTY_MAKEUP, null, train), "makeup source");
            t.start();
            JUnitUtil.waitFor(() -> !t.isAlive(), "source thread ends");
        }

        int listeners() {
            return getPropertyChangeListeners(PROPERTY_MAKEUP).length;
        }
    }

    private static TrainMakeup makeup(TrainComponent... components) {
        return new TrainMakeup(List.of(components),
                List.of(new TrainMakeupSource.Work("North Industries", 1, 2)));
    }

    private static final TrainComponent ON = new TrainComponent("Southport", 300.0);
    private static final TrainComponent RAKE = new TrainComponent("North Industries", 100.0);

    @Test
    public void testRecordTakesMakeupWhenMade() {
        Source source = new Source("STF", makeup(ON, RAKE));
        InstanceManager.store(source, TrainMakeupSource.class);
        assertEquals(0, source.listeners(), "no record, so the source isn't listened to");

        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("STF"));
        TrainRecord other = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("Other"));
        assertEquals(1, source.listeners(), "listened to while there is a record");
        assertEquals(List.of(ON, RAKE), r.getTrainComponents());
        assertEquals(400.0, r.getLength(LengthSource.OPERATIONS));
        assertEquals(1, other.getTrainComponents().size(), "a train no source knows is left as it is");
        assertNull(other.getLength(LengthSource.OPERATIONS));

        ThreadingUtil.runOnLayout(() -> {
            manager.remove(r);
            manager.remove(other);
        });
        assertEquals(0, source.listeners(), "not listened to once there is no record");
    }

    @Test
    public void testMakeupChanges() {
        Source source = new Source("STF", makeup(ON, RAKE));
        InstanceManager.store(source, TrainMakeupSource.class);
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("STF"));

        source.makeup = makeup(ON); // the rake set out
        source.changed();
        JUnitUtil.waitFor(() -> r.getTrainComponents().size() == 1, "the rake set out");
        assertEquals(List.of(ON), r.getTrainComponents());
        assertEquals(300.0, r.getLength(LengthSource.OPERATIONS));

        source.makeup = makeup(ON, new TrainComponent("North Industries", null));
        source.changed();
        JUnitUtil.waitFor(() -> r.getTrainComponents().size() == 2, "a car of no known length picked up");
        assertNull(r.getLength(LengthSource.OPERATIONS), "the whole length isn't known");

        source.makeup = null; // the train terminated, or was reset
        source.changed();
        ThreadingUtil.runOnLayout(() -> {
            // the change, handed to the layout thread (Swing thread) before this, has been taken
        });
        assertEquals(2, r.getTrainComponents().size(), "the components are left as they were");
    }

    @Test
    public void testOperationsLengthClearedWhenSourceForgets() {
        Source source = new Source("STF", makeup(ON, RAKE));
        InstanceManager.store(source, TrainMakeupSource.class);
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("STF"));
        assertEquals(400.0, r.getLength(LengthSource.OPERATIONS));

        source.makeup = null;
        source.changed();
        JUnitUtil.waitFor(() -> r.getLength(LengthSource.OPERATIONS) == null, "Operations length cleared");
        assertEquals(List.of(ON, RAKE), r.getTrainComponents(), "the components are left as they were");
    }

    @Test
    public void testRenamedRecordTakesItsNewTrainsMakeup() {
        Source source = new Source("STF", makeup(ON, RAKE));
        InstanceManager.store(source, TrainMakeupSource.class);
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("Extra"));
        assertNull(r.getLength(LengthSource.OPERATIONS));

        ThreadingUtil.runOnLayout(() -> r.setName("STF"));
        assertEquals(List.of(ON, RAKE), r.getTrainComponents());
        assertEquals(400.0, r.getLength(LengthSource.OPERATIONS));

        ThreadingUtil.runOnLayout(() -> r.setName("Extra 2"));
        assertNull(r.getLength(LengthSource.OPERATIONS), "no source knows its new name");
    }

    @Test
    public void testSourceStoredLaterAndRemoved() {
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("STF"));
        Source source = new Source("STF", makeup(ON, RAKE));
        ThreadingUtil.runOnLayout(() -> InstanceManager.store(source, TrainMakeupSource.class));
        JUnitUtil.waitFor(() -> r.getTrainComponents().size() == 2, "the later source's makeup taken");
        assertEquals(1, source.listeners());

        ThreadingUtil.runOnLayout(() -> InstanceManager.deregister(source, TrainMakeupSource.class));
        JUnitUtil.waitFor(() -> source.listeners() == 0, "a removed source isn't listened to");
    }

    @Test
    public void testDisposeStopsListening() {
        Source source = new Source("STF", makeup(ON, RAKE));
        InstanceManager.store(source, TrainMakeupSource.class);
        ThreadingUtil.runOnLayout(() -> manager.newRecord("STF"));
        assertEquals(1, source.listeners());
        ThreadingUtil.runOnLayout(() -> ((jmri.Disposable) manager).dispose());
        assertEquals(0, source.listeners());
    }

    @Test
    public void testChangeArrivingAfterDisposeIsIgnored() {
        Source source = new Source("STF", makeup(ON, RAKE));
        InstanceManager.store(source, TrainMakeupSource.class);
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("STF"));
        source.makeup = makeup(ON);
        ThreadingUtil.runOnLayout(() -> {
            // the change is handed to the layout thread (Swing thread), so it is taken only after this
            Thread t = new Thread(() -> source.firePropertyChange(TrainMakeupSource.PROPERTY_MAKEUP, null, "STF"),
                    "makeup source");
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ((jmri.Disposable) manager).dispose();
        });
        ThreadingUtil.runOnLayout(() -> {
            // the change has been taken
        });
        assertEquals(List.of(ON, RAKE), r.getTrainComponents(), "not taken once the manager is disposed");
    }

    @Test
    public void testChangeWithoutATrainNameIsIgnored() {
        Source source = new Source("STF", makeup(ON, RAKE));
        InstanceManager.store(source, TrainMakeupSource.class);
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("STF"));
        source.makeup = makeup(ON);
        ThreadingUtil.runOnLayout(() -> source.firePropertyChange(TrainMakeupSource.PROPERTY_MAKEUP, null, null));
        assertEquals(List.of(ON, RAKE), r.getTrainComponents());
    }

    @Test
    public void testMakeupHasAComponent() {
        List<TrainComponent> none = List.of();
        List<TrainMakeupSource.Work> noWork = List.of();
        assertThrows(IllegalArgumentException.class, () -> new TrainMakeup(none, noWork));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        manager = InstanceManager.getDefault(TrainRecordManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
