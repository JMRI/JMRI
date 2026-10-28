package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jmri.Block;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.implementation.DefaultSection;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that the Block state changes in an auto train's Allocated Sections
 * are all handled, one at a time, in the order they happened, with the state
 * each Block changed to.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AllocatedSectionBlockEventsTest {

    private DispatcherFrame dispatcher;
    private ActiveTrain at;
    private final List<AllocatedSection> sections = new ArrayList<>();
    private AutoActiveTrain.AutoEngineer engineer = null;

    /**
     * An auto train that records the Block changes it is given, taking a
     * little time over each so that later changes arrive while it is busy.
     */
    private static class RecordingTrain extends AutoActiveTrain {

        final List<String> handled = Collections.synchronizedList(new ArrayList<>());

        RecordingTrain(ActiveTrain at) {
            super(at);
        }

        @Override
        protected void handleBlockStateChange(AllocatedSection as, Block b, int state) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            handled.add(b.getUserName() + (state == Block.OCCUPIED ? " occupied" : " unoccupied"));
        }
    }

    /**
     * An auto train whose signal handling is left out, so that only its Block
     * tracking runs.
     */
    private static class TrackingTrain extends AutoActiveTrain {

        TrackingTrain(ActiveTrain at) {
            super(at);
        }

        @Override
        protected synchronized void setupNewCurrentSignal(AllocatedSection as, boolean forceSpeedChange) {
            // no signals in this test
        }
    }

    private Block block(String name) {
        Block b = new Block("IB" + name, name);
        b.setState(Block.UNOCCUPIED);
        return b;
    }

    private AllocatedSection allocate(String name, int seq, Block... blocks) {
        Section s = new DefaultSection(name);
        for (Block b : blocks) {
            s.addBlock(b);
        }
        s.setState(Section.FORWARD);
        AllocatedSection as = new AllocatedSection(s, at, seq, null, 0);
        at.addAllocatedSection(as);
        sections.add(as);
        return as;
    }

    private void setField(AutoActiveTrain aat, String name, Object value) throws ReflectiveOperationException {
        Field f = AutoActiveTrain.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(aat, value);
    }

    private void waitForQueue() {
        JUnitUtil.waitFor(() -> at.isBlockStateChangeQueueIdle(), "Block changes handled");
    }

    @Test
    public void testShortPulseOnOneBlockBothEdgesHandled() {
        RecordingTrain aat = new RecordingTrain(at);
        Block b1 = block("B1");
        Block b2 = block("B2");
        allocate("TS1", 1, b1, b2);

        // a short Block crossed at speed, and a bounce
        b1.setState(Block.OCCUPIED);
        b1.setState(Block.UNOCCUPIED);
        b1.setState(Block.OCCUPIED);
        b1.setState(Block.UNOCCUPIED);

        JUnitUtil.waitFor(() -> aat.handled.size() >= 4, "all four changes handled");
        waitForQueue();
        assertEquals(Arrays.asList("B1 occupied", "B1 unoccupied", "B1 occupied", "B1 unoccupied"),
                aat.handled, "every change handled, in order, with the state it changed to");
    }

    @Test
    public void testChangesAcrossBlocksAndSectionsHandledInOrder() {
        RecordingTrain aat = new RecordingTrain(at);
        Block b1 = block("B1");
        Block b2 = block("B2");
        Block b3 = block("B3");
        Block b4 = block("B4");
        allocate("TS1", 1, b1, b2);
        allocate("TS2", 2, b3, b4);

        b1.setState(Block.OCCUPIED);
        b2.setState(Block.OCCUPIED);
        b1.setState(Block.UNOCCUPIED);
        b3.setState(Block.OCCUPIED);
        b4.setState(Block.OCCUPIED);
        b2.setState(Block.UNOCCUPIED);
        b3.setState(Block.UNOCCUPIED);

        JUnitUtil.waitFor(() -> aat.handled.size() >= 7, "all seven changes handled");
        waitForQueue();
        assertEquals(Arrays.asList("B1 occupied", "B2 occupied", "B1 unoccupied", "B3 occupied",
                "B4 occupied", "B2 unoccupied", "B3 unoccupied"),
                aat.handled, "changes handled in the order they happened");
    }

    /**
     * A train that has reached its destination and is stopping there is DONE
     * but not terminated: changes after that, such as its tail clearing a
     * Block, must still be handled.
     */
    @Test
    public void testChangesHandledWhileStoppingAtDestination() {
        RecordingTrain aat = new RecordingTrain(at);
        Block b1 = block("B1");
        Block b2 = block("B2");
        allocate("TS1", 1, b1, b2);
        b1.setState(Block.OCCUPIED);
        b2.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> aat.handled.size() >= 2, "entries handled");
        waitForQueue();

        at.setStatus(ActiveTrain.DONE);
        b1.setState(Block.UNOCCUPIED);   // tail clears

        JUnitUtil.waitFor(() -> aat.handled.size() >= 3, "tail clearing handled");
        waitForQueue();
        assertEquals(Arrays.asList("B1 occupied", "B2 occupied", "B1 unoccupied"),
                aat.handled, "change after DONE handled");
    }

    @Test
    public void testChangesDroppedOnceTerminated() {
        RecordingTrain aat = new RecordingTrain(at);
        Block b1 = block("B1");
        allocate("TS1", 1, b1);

        at.setMode(ActiveTrain.TERMINATED);
        b1.setState(Block.OCCUPIED);
        waitForQueue();
        assertEquals(Collections.emptyList(), aat.handled, "terminated train ignores changes");
    }

    /**
     * With strict train tracking, the head entering the next Block and then
     * the one after it within a millisecond must be followed, not taken as a
     * Block ahead entered illegally.
     */
    @Test
    public void testStrictTrackingFollowsFastBlockEntries() throws ReflectiveOperationException, InterruptedException {
        when(dispatcher.getUseStrictTrainTracking()).thenReturn(true);
        TrackingTrain aat = new TrackingTrain(at);
        engineer = aat.new AutoEngineer(mock(DccThrottle.class), null);
        setField(aat, "_autoEngineer", engineer);
        setField(aat, "_dispatcher", dispatcher);

        Block z = block("Z");   // the rest of the train, stays occupied
        Block a = block("A");
        Block b = block("B");
        Block c = block("C");
        Block d = block("D");
        z.setState(Block.OCCUPIED);
        allocate("TS1", 1, z, a, b, c, d);
        waitForQueue();

        for (int i = 0; i < 200; i++) {
            setField(aat, "_currentBlock", z);
            setField(aat, "_previousBlock", null);
            setField(aat, "_nextBlock", a);

            a.setState(Block.OCCUPIED);
            if (i % 2 == 1) {
                Thread.sleep(1);
            }
            b.setState(Block.OCCUPIED);

            waitForQueue();
            assertNotEquals(ActiveTrain.STOPPED, at.getStatus(), "train stopped on pass " + i);
            assertSame(b, aat.getCurrentBlock(), "head in B on pass " + i);
            assertSame(c, aat.getNextBlock(), "next Block C on pass " + i);

            a.setState(Block.UNOCCUPIED);
            b.setState(Block.UNOCCUPIED);
            waitForQueue();
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        dispatcher = mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, dispatcher);
        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        at.setDispatcher(dispatcher);
    }

    @AfterEach
    public void tearDown() {
        waitForQueue();
        for (AllocatedSection as : sections) {
            as.dispose();
        }
        sections.clear();
        if (engineer != null) {
            engineer.abort();
            engineer = null;
        }
        at.dispose();
        JUnitUtil.tearDown();
    }

}
