package jmri.jmrit.dispatcher;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import jmri.Section;
import jmri.Transit;
import jmri.TransitSection;
import jmri.implementation.DefaultSection;
import jmri.implementation.DefaultTransit;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that {@link ActiveTrain#allocateAFresh} is not run again for the same
 * train while it is waiting for the auto allocator, as can happen when other
 * GUI events, such as a second Restart, are handled during that wait.
 */
public class ActiveTrainAllocateAFreshTest {

    @Test
    public void testRestartWhileRestartingIsIgnored() {
        Transit transit = new DefaultTransit("TT1");
        Section s = new DefaultSection("TS1");
        transit.addTransitSection(new TransitSection(s, 1, Section.FORWARD));
        DispatcherFrame df = mock(DispatcherFrame.class);
        when(df.getAllocatedSectionsList()).thenReturn(new ArrayList<>());
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        at.setDispatcher(df);
        // a second Restart is handled while the first one waits for the allocator
        doAnswer(invocation -> {
            at.allocateAFresh();
            return null;
        }).doNothing().when(df).queueWaitForEmpty();

        at.allocateAFresh();

        verify(df, times(1)).allocateNewActiveTrain(at);

        // once done, the train can be restarted again
        at.allocateAFresh();
        verify(df, times(2)).allocateNewActiveTrain(at);
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
