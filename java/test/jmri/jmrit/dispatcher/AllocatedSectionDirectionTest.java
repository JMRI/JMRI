package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.implementation.DefaultSection;
import jmri.implementation.DefaultTransit;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * An Allocated Section gives the direction it is allocated in, also once its
 * Section has been released, so that a Block change in it that is still being
 * handled finds the next Block the way the train was going.
 */
public class AllocatedSectionDirectionTest {

    private ActiveTrain at;
    private Section section;
    private AllocatedSection allocated;

    @Test
    public void testDirectionKeptAfterRelease() {
        section.setState(Section.REVERSE);
        assertEquals(Section.REVERSE, allocated.getDirection(), "the direction it is allocated in");

        section.setState(Section.FREE);
        assertEquals(Section.REVERSE, allocated.getDirection(), "still the direction it was allocated in once released");
    }

    @Test
    public void testDirectionFollowsReversal() {
        section.setState(Section.FORWARD);
        assertEquals(Section.FORWARD, allocated.getDirection(), "allocated forward");

        section.setState(Section.REVERSE);
        assertEquals(Section.REVERSE, allocated.getDirection(), "the train reversed");
    }

    @Test
    public void testNoDirectionYet() {
        assertEquals(Section.FREE, allocated.getDirection(), "not yet given a direction: the Section's state");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        DispatcherFrame dispatcher = mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, dispatcher);
        Transit transit = new DefaultTransit("TT1");
        at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        at.setDispatcher(dispatcher);
        section = new DefaultSection("TS1");
        allocated = new AllocatedSection(section, at, 1, null, 0);
    }

    @AfterEach
    public void tearDown() {
        allocated.dispose();
        at.dispose();
        JUnitUtil.tearDown();
    }

}
