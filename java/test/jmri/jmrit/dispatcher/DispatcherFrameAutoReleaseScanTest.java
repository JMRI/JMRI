package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests that {@link DispatcherFrame#checkAutoRelease} releases every exited
 * Section behind each train, in allocation order, without rescanning all
 * allocated Sections for every candidate and after every release.
 */
@DisabledIfHeadless
public class DispatcherFrameAutoReleaseScanTest {

    private static final int TRAINS = 10;
    private static final int SECTIONS = 6; // per train
    private static final int HEAD = 4; // the head of each train is in its fourth Section

    private LayoutEditor le;
    private DispatcherFrame d;
    private final List<List<AllocatedSection>> trains = new ArrayList<>();
    private volatile Thread testThread;
    private final AtomicInteger trainLookups = new AtomicInteger();

    /*
     * A train with Sections allocated in order 1 to SECTIONS, the head in
     * Section HEAD, the Sections behind it exited, plus one extra Section
     * that has been exited.
     */
    private void addTrain(int t) {
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn("T" + t);
        when(at.getActiveTrainName()).thenReturn("T" + t);
        when(at.getStatus()).thenReturn(ActiveTrain.RUNNING);
        when(at.getMode()).thenReturn(ActiveTrain.DISPATCHED);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_WHOLETRAIN);
        Transit transit = mock(Transit.class);
        when(transit.getSystemName()).thenReturn("IT" + t);
        when(at.getTransit()).thenReturn(transit);
        List<AllocatedSection> list = new ArrayList<>();
        when(at.getAllocatedSectionList()).thenAnswer(invocation -> new ArrayList<>(list));
        List<Section> sections = new ArrayList<>();
        for (int n = 1; n <= SECTIONS; n++) {
            Section s = mock(Section.class);
            when(s.getDisplayName()).thenReturn("T" + t + "S" + n);
            when(s.getSystemName()).thenReturn("IYT" + t + "S" + n);
            when(s.getOccupancy()).thenReturn(n == HEAD ? Section.OCCUPIED : Section.UNOCCUPIED);
            sections.add(s);
        }
        for (int n = 1; n <= SECTIONS; n++) {
            list.add(allocatedSection(at, sections.get(n - 1), n, n < SECTIONS ? sections.get(n) : null,
                    n < HEAD, n <= HEAD));
        }
        Section extra = mock(Section.class);
        when(extra.getOccupancy()).thenReturn(Section.UNOCCUPIED);
        list.add(allocatedSection(at, extra, -1, null, true, true));
        trains.add(list);
    }

    private AllocatedSection allocatedSection(ActiveTrain at, Section s, int allocationNumber,
            Section next, boolean exited, boolean entered) {
        AllocatedSection as = mock(AllocatedSection.class);
        // count the lookups made by the release check, not by the tables on the GUI thread
        when(as.getActiveTrain()).thenAnswer(invocation -> {
            if (Thread.currentThread() == testThread) {
                trainLookups.incrementAndGet();
            }
            return at;
        });
        when(as.getSection()).thenReturn(s);
        String name = s.getDisplayName();
        when(as.getSectionName()).thenReturn(name);
        when(as.getAllocationNumber()).thenReturn(allocationNumber);
        when(as.getNextSection()).thenReturn(next);
        when(as.getExited()).thenReturn(exited);
        when(as.getEntered()).thenReturn(entered);
        return as;
    }

    @Test
    public void testExitedSectionsReleasedWithoutRescanning() {
        for (int t = 0; t < TRAINS; t++) {
            addTrain(t);
        }
        // the trains' Sections are interleaved in the list, as when they run together
        List<AllocatedSection> before = new ArrayList<>();
        for (int n = 0; n <= SECTIONS; n++) {
            for (List<AllocatedSection> list : trains) {
                before.add(list.get(n));
            }
        }
        d.getAllocatedSectionsList().addAll(before);

        testThread = Thread.currentThread();
        d.checkAutoRelease();
        testThread = null;

        for (AllocatedSection as : before) {
            boolean expectReleased = as.getAllocationNumber() != -1 && as.getAllocationNumber() < HEAD;
            assertEquals(expectReleased, !d.getAllocatedSectionsList().contains(as),
                    as.getSectionName() + (expectReleased ? " released" : " kept"));
        }
        assertEquals(TRAINS * (SECTIONS - HEAD + 2), d.getAllocatedSectionsList().size(),
                "the head, the Sections ahead and the extra Section of each train are kept");

        // a few looks at each Section for each pass, and a pass for each Section released per train
        int limit = 4 * before.size() * HEAD;
        assertTrue(trainLookups.get() <= limit,
                "trains of allocated Sections looked up " + trainLookups.get() + " times, limit " + limit);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        // auto release on
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        le = new LayoutEditor("Auto Release Scan Test");
        d = InstanceManager.getDefault(DispatcherFrame.class);
        d.setAutoAllocate(false);
        d.openDispatcherWindow();
    }

    @AfterEach
    public void tearDown() {
        if (d != null) {
            d.getAllocatedSectionsList().clear();
            JUnitUtil.dispose(d);
        }
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        // do not leave auto allocate on for tests that run after this one
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
