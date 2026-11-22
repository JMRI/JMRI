package jmri.jmrit.dispatcher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Path;
import jmri.Section;
import jmri.Transit;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * A train waiting for a Section that only its own tail occupies is reported,
 * once, and the Section is still not allocated.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class DispatcherFrameOwnTailTest {

    private LayoutEditor le;
    private DispatcherFrame d;
    private ActiveTrain train;
    private Block here;   // the train's head, in its allocated Section
    private Block tail;   // the Block behind, holding its tail
    private Section next;  // the Section the train's route goes on into, holding tail

    @Test
    public void testOwnTailIsReportedOnce() throws JmriException {
        here.setState(Block.OCCUPIED);
        tail.setState(Block.OCCUPIED);
        AllocationRequest ar = new AllocationRequest(next, 2, Section.FORWARD, train);
        Assertions.assertNull(d.allocateSection(ar, null), "an occupied Section is still not allocated");
        JUnitAppender.assertWarnMessageStartsWith(
                "Train: Section Next can't be allocated, as the train's own tail occupies it (Block Tail joins");
        Assertions.assertNull(d.allocateSection(ar, null), "still not allocated");
        // reported once for the train and the Section: a second warning would fail the test at tear-down
    }

    @Test
    public void testAnotherTrainWithAGapIsNotReported() throws JmriException {
        here.setState(Block.OCCUPIED);
        tail.setState(Block.OCCUPIED);
        here.removePath(here.getPaths().get(0));  // no way from the train's Block to the tail's: not joined
        AllocationRequest ar = new AllocationRequest(next, 2, Section.FORWARD, train);
        Assertions.assertNull(d.allocateSection(ar, null), "an occupied Section is not allocated");
        // no warning: one would fail the test at tear-down
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        le = new LayoutEditor("Own Tail Test");
        d = InstanceManager.getDefault(DispatcherFrame.class);
        d.setAutoAllocate(true);

        here = new Block("IB_HERE", "Here");
        tail = new Block("IB_TAIL", "Tail");
        here.addPath(new Path(tail, Path.NONE, Path.NONE));
        tail.addPath(new Path(here, Path.NONE, Path.NONE));
        Section current = mock(Section.class);
        List<Block> currentBlocks = new ArrayList<>(List.of(here));
        when(current.getBlockList()).thenReturn(currentBlocks);
        next = mock(Section.class);
        List<Block> nextBlocks = new ArrayList<>(List.of(tail));
        when(next.getBlockList()).thenReturn(nextBlocks);
        when(next.getState()).thenReturn(Section.FREE);
        when(next.getOccupancy()).thenReturn(Section.OCCUPIED);
        when(next.getDisplayName(jmri.NamedBean.DisplayOptions.USERNAME_SYSTEMNAME)).thenReturn("Next");

        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(current);
        train = mock(ActiveTrain.class);
        when(train.getTrainName()).thenReturn("Train");
        when(train.getActiveTrainName()).thenReturn("Train");
        when(train.getNextTrain()).thenReturn("");
        when(train.getTransit()).thenReturn(mock(Transit.class));
        when(train.getAllocatedSectionList()).thenReturn(new ArrayList<>(List.of(as)));
        when(train.getLastAllocatedSection()).thenReturn(current);
        when(train.getStartBlock()).thenReturn(here);
    }

    @AfterEach
    public void tearDown() {
        if (d != null) {
            JUnitUtil.dispose(d);
        }
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
