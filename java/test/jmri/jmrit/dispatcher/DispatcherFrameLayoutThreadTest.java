package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.InstanceManager;
import jmri.Section;
import jmri.implementation.DefaultSection;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests that the Dispatcher changes a Section's state on the layout thread,
 * also when the auto allocator's thread allocates or releases it, so that the
 * Section's listeners run there.
 */
@DisabledIfHeadless
public class DispatcherFrameLayoutThreadTest {

    private LayoutEditor le;
    private DispatcherFrame d;

    private static void runOnOtherThread(ThreadingUtil.ThreadActionWithJmriException action) throws Exception {
        List<Exception> failed = new ArrayList<>();
        Thread t = new Thread(() -> {
            try {
                action.run();
            } catch (Exception e) {
                failed.add(e);
            }
        }, "allocator stand-in");
        t.start();
        t.join(10000);
        if (!failed.isEmpty()) {
            throw failed.get(0);
        }
    }

    // the Dispatcher's tables may show the train while the test runs
    private static ActiveTrain mockTrain() {
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn("Train");
        when(at.getTransit()).thenReturn(new jmri.implementation.DefaultTransit("TT1"));
        return at;
    }

    @Test
    public void testSectionAllocatedOnLayoutThread() throws Exception {
        Section section = new DefaultSection("TS1");
        List<Boolean> changedOnLayoutThread = Collections.synchronizedList(new ArrayList<>());
        section.addPropertyChangeListener("state",
                e -> changedOnLayoutThread.add(ThreadingUtil.isLayoutThread()));
        ActiveTrain at = mockTrain();

        Method allocate = DispatcherFrame.class.getDeclaredMethod("allocateSection", ActiveTrain.class,
                Section.class, int.class, Section.class, int.class, int.class, List.class);
        allocate.setAccessible(true);
        runOnOtherThread(() -> {
            try {
                allocate.invoke(d, at, section, 1, null, 0, Section.FORWARD, null);
            } catch (ReflectiveOperationException e) {
                throw new jmri.JmriException(e);
            }
        });

        assertEquals(Section.FORWARD, section.getState(), "Section allocated");
        assertEquals(List.of(true), changedOnLayoutThread, "Section state set on the layout thread");
    }

    @Test
    public void testSectionReleasedOnLayoutThread() throws Exception {
        List<Boolean> releasedOnLayoutThread = Collections.synchronizedList(new ArrayList<>());
        ActiveTrain at = mockTrain();
        doAnswer(invocation -> {
            releasedOnLayoutThread.add(ThreadingUtil.isLayoutThread());
            return null;
        }).when(at).releaseAllocatedSection(org.mockito.ArgumentMatchers.any());
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getActiveTrain()).thenReturn(at);
        when(as.getSection()).thenReturn(new DefaultSection("TS2"));
        ThreadingUtil.runOnLayout(() -> d.getAllocatedSectionsList().add(as));

        runOnOtherThread(() -> d.doReleaseAllocatedSection(as, true));

        assertEquals(List.of(true), releasedOnLayoutThread, "Section released on the layout thread");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        le = new LayoutEditor("Layout Thread Test");
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
