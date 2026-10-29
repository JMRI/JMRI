package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicReference;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.TransitSection;
import jmri.implementation.DefaultSection;
import jmri.implementation.DefaultTransit;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that a train starting back, which sets the direction of each of its
 * Sections, cannot give a direction to a Section that is being released.
 * <p>
 * The release sets the Section free and then takes it off the train. The test
 * starts the train reversing at the moment the Section has been set free,
 * before it has been taken off the train, and waits until the reversing
 * thread has either finished or is held up.
 *
 * @author Keith Bruce 2026
 */
public class ActiveTrainReleaseReverseTest {

    private ActiveTrain at;
    private Section section1;
    private Section section2;

    private AllocatedSection allocate(Section s, int seq) {
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(s);
        when(as.getSequence()).thenReturn(seq);
        when(as.getAllocationNumber()).thenReturn(seq);
        at.addAllocatedSection(as);
        s.setState(Section.FORWARD);
        return as;
    }

    @Test
    public void testTrainStartingBackDuringAReleaseLeavesTheSectionFree() {
        AllocatedSection behind = allocate(section1, 1);
        allocate(section2, 2);

        AtomicReference<Thread> reversing = new AtomicReference<>();
        PropertyChangeListener l = e -> {
            if (Section.PROPERTY_STATE.equals(e.getPropertyName())
                    && Integer.valueOf(Section.FREE).equals(e.getNewValue())
                    && reversing.get() == null) {
                // the train starts back now, in the middle of the release
                Thread t = new Thread(() -> at.reverseAllAllocatedSections(), "reverse train");
                reversing.set(t);
                t.start();
                JUnitUtil.waitFor(() -> t.getState() == Thread.State.BLOCKED
                        || t.getState() == Thread.State.TERMINATED, "reversing thread finished or held up");
            }
        };
        section1.addPropertyChangeListener(l);

        at.releaseAllocatedSection(behind);
        section1.removePropertyChangeListener(l);
        JUnitUtil.waitFor(() -> !reversing.get().isAlive(), "reversing thread finished");

        assertFalse(at.getAllocatedSectionList().contains(behind), "the released Section is off the train");
        assertEquals(Section.FREE, section1.getState(), "the released Section stays free");
        assertEquals(Section.REVERSE, section2.getState(), "the train's own Section is reversed");
    }

    @Test
    public void testReleaseWhileTrainStartsBackLeavesTheSectionFree() {
        allocate(section1, 1);
        AllocatedSection ahead = allocate(section2, 2);

        AtomicReference<Thread> releasing = new AtomicReference<>();
        PropertyChangeListener l = e -> {
            if (Section.PROPERTY_STATE.equals(e.getPropertyName())
                    && Integer.valueOf(Section.REVERSE).equals(e.getNewValue())
                    && releasing.get() == null) {
                // Section 2 is released now, while the train is reversing its Sections
                Thread t = new Thread(() -> at.releaseAllocatedSection(ahead), "release");
                releasing.set(t);
                t.start();
                JUnitUtil.waitFor(() -> t.getState() == Thread.State.BLOCKED
                        || t.getState() == Thread.State.TERMINATED, "release finished or held up");
            }
        };
        section1.addPropertyChangeListener(l);

        at.reverseAllAllocatedSections();
        section1.removePropertyChangeListener(l);
        JUnitUtil.waitFor(() -> !releasing.get().isAlive(), "release finished");

        assertFalse(at.getAllocatedSectionList().contains(ahead), "the released Section is off the train");
        assertEquals(Section.FREE, section2.getState(), "the released Section ends free");
        assertEquals(Section.REVERSE, section1.getState(), "the train's own Section is reversed");
    }

    @Test
    public void testReleaseAfterReversingSetsFree() {
        AllocatedSection behind = allocate(section1, 1);
        allocate(section2, 2);

        at.reverseAllAllocatedSections();
        assertEquals(Section.REVERSE, section1.getState(), "reversed with the train");

        at.releaseAllocatedSection(behind);
        assertEquals(Section.FREE, section1.getState(), "free once released");
        assertEquals(1, at.getAllocatedSectionList().size(), "one Section left on the train");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLayoutBlockManager();

        Block b1 = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("IB1", "Block 1");
        Block b2 = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("IB2", "Block 2");
        section1 = new DefaultSection("TS1");
        section1.addBlock(b1);
        section2 = new DefaultSection("TS2");
        section2.addBlock(b2);
        Transit transit = new DefaultTransit("TT1");
        transit.addTransitSection(new TransitSection(section1, 1, Section.FORWARD));
        transit.addTransitSection(new TransitSection(section2, 2, Section.FORWARD));

        DispatcherFrame df = mock(DispatcherFrame.class);
        when(df.requestAllocation(any(), any(), anyInt(), anyInt(), anyBoolean(), any(), anyBoolean()))
                .thenReturn(true);
        at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        at.setDispatcher(df);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
