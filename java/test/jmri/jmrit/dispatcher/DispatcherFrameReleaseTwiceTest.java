package jmri.jmrit.dispatcher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jmri.InstanceManager;
import jmri.Section;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * A release queued for a Section that has already been released, for example
 * when a train is terminated while an automatic release is queued, must not
 * touch the Section again: it may be allocated to another train by then.
 */
@DisabledIfHeadless
public class DispatcherFrameReleaseTwiceTest {

    private LayoutEditor le;
    private DispatcherFrame d;

    @Test
    public void testSecondReleaseLeavesSectionAlone() {
        Section section = mock(Section.class);
        ActiveTrain train = mock(ActiveTrain.class);
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(section);
        when(as.getActiveTrain()).thenReturn(train);
        when(as.getSectionName()).thenReturn("Test Section");

        // already released: not in the allocated Sections list any more
        d.doReleaseAllocatedSection(as, true);

        verify(section, never()).setState(Section.FREE);
        verify(train, never()).removeAllocatedSection(as);
        verify(as, never()).dispose();
    }

    @Test
    public void testFirstReleaseFreesSection() {
        Section section = mock(Section.class);
        ActiveTrain train = mock(ActiveTrain.class);
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(section);
        when(as.getActiveTrain()).thenReturn(train);
        when(as.getSectionName()).thenReturn("Test Section");
        d.getAllocatedSectionsList().add(as);

        d.doReleaseAllocatedSection(as, true);

        verify(section).setState(Section.FREE);
        verify(train).removeAllocatedSection(as);
        verify(as).dispose();
        Assertions.assertFalse(d.getAllocatedSectionsList().contains(as));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        le = new LayoutEditor("Release Twice Test");
        d = InstanceManager.getDefault(DispatcherFrame.class);
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
        JUnitUtil.tearDown();
    }

}
