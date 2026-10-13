package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import jmri.Section;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the final auto-release check of an exited Section,
 * {@link DispatcherFrame#sectionNotRequiredByHeadOnly}.
 * <p>
 * Each test uses a train running through Sections A, B, C, D in that order.
 */
public class DispatcherFrameAutoReleaseTest {

    private ActiveTrain at;
    private final List<Section> sections = new ArrayList<>();
    private final List<AllocatedSection> allocated = new ArrayList<>();

    /**
     * Build Sections A to D.
     * @param lengthMM length of each Section
     */
    private void buildTransit(int lengthMM) {
        at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn("Test");
        when(at.getAllocatedSectionList()).thenReturn(allocated);
        for (String name : new String[]{"A", "B", "C", "D"}) {
            Section s = mock(Section.class);
            when(s.getDisplayName()).thenReturn(name);
            when(s.getActualLength()).thenReturn(lengthMM);
            when(s.getOccupancy()).thenReturn(Section.UNOCCUPIED);
            sections.add(s);
            AllocatedSection as = mock(AllocatedSection.class);
            when(as.getSection()).thenReturn(s);
            when(as.getSectionName()).thenReturn(name);
            allocated.add(as);
        }
        for (int i = 0; i < allocated.size() - 1; i++) {
            when(allocated.get(i).getNextSection()).thenReturn(sections.get(i + 1));
        }
    }

    /**
     * Place the detected head of the train in a Section; all earlier
     * Sections have been entered and exited.
     * @param index Section index (0 = A)
     */
    private void headIn(int index) {
        for (int i = 0; i < allocated.size(); i++) {
            when(allocated.get(i).getExited()).thenReturn(i < index);
            when(sections.get(i).getOccupancy()).thenReturn(i == index ? Section.OCCUPIED : Section.UNOCCUPIED);
        }
    }

    private AutoActiveTrain autoTrain(long trainLengthMM) {
        AutoActiveTrain aat = mock(AutoActiveTrain.class);
        AutoActiveTrain.AutoEngineer engineer = mock(AutoActiveTrain.AutoEngineer.class);
        when(engineer.isStopped()).thenReturn(false);
        when(aat.getAutoEngineer()).thenReturn(engineer);
        when(aat.getMaxTrainLengthMM()).thenReturn(trainLengthMM);
        when(at.getAutoActiveTrain()).thenReturn(aat);
        return aat;
    }

    private boolean releaseA() {
        return DispatcherFrame.sectionNotRequiredByHeadOnly(true, at, allocated.get(0));
    }

    @Test
    public void testEarlierHoldDecisionIsKept() {
        buildTransit(1000);
        headIn(2);
        for (TrainDetection td : TrainDetection.values()) {
            when(at.getTrainDetection()).thenReturn(td);
            assertFalse(DispatcherFrame.sectionNotRequiredByHeadOnly(false, at, allocated.get(0)),
                    "a Section already held must stay held for " + td);
        }
    }

    @Test
    public void testWholeTrainReleasedOnceExited() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_WHOLETRAIN);
        headIn(1);
        assertTrue(releaseA());
    }

    @Test
    public void testHeadOnlyDispatchedTrainHeldUntilNextSectionExited() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADONLY);
        when(at.getAutoActiveTrain()).thenReturn(null);
        headIn(1);
        assertFalse(releaseA(), "head in B, tail may still be in A");
        headIn(2);
        assertTrue(releaseA(), "head in C, B exited");
    }

    @Test
    public void testHeadOnlyAutoTrainWithoutLengthHeldUntilNextSectionExited() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADONLY);
        autoTrain(0);
        headIn(1);
        assertFalse(releaseA(), "head in B, tail may still be in A");
        headIn(2);
        assertTrue(releaseA(), "head in C, B exited");
    }

    @Test
    public void testHeadOnlyAutoTrainReleasedByLength() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADONLY);
        autoTrain(800);
        headIn(1);
        assertFalse(releaseA(), "head in B, moving");
        headIn(2);
        assertTrue(releaseA(), "B is longer than the train");
    }

    @Test
    public void testHeadOnlyAutoTrainLongerThanSection() {
        buildTransit(500);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADONLY);
        autoTrain(800);
        headIn(2);
        assertFalse(releaseA(), "only B (500) exited beyond A, train is 800");
        headIn(3);
        assertTrue(releaseA(), "B and C (1000) exited beyond A");
    }

    @Test
    public void testHeadOnlyAutoTrainWithoutEngineer() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADONLY);
        AutoActiveTrain aat = autoTrain(800);
        when(aat.getAutoEngineer()).thenReturn(null);
        headIn(2);
        assertTrue(releaseA());
    }

    private boolean tailHasLeftA() {
        return DispatcherFrame.tailHasLeftSection(at, allocated.get(0), allocated.get(1));
    }

    @Test
    public void testHeadAndTailReleasedWhenTailInNextSection() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        headIn(2);
        when(sections.get(1).getOccupancy()).thenReturn(Section.OCCUPIED); // tail in B
        when(allocated.get(1).getClearedOrder()).thenReturn(1L); // head left B earlier
        when(allocated.get(0).getClearedOrder()).thenReturn(2L); // then tail left A
        assertTrue(tailHasLeftA());
    }

    @Test
    public void testHeadAndTailReleasedWhenTailPassedThroughShortSection() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        headIn(2);
        when(allocated.get(0).getClearedOrder()).thenReturn(2L); // tail left A
        when(allocated.get(1).getClearedOrder()).thenReturn(3L); // then tail left B
        assertTrue(tailHasLeftA());
    }

    @Test
    public void testHeadAndTailHeldWhenTailLostDetection() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        when(at.getAutoActiveTrain()).thenReturn(null);
        headIn(2);
        when(allocated.get(1).getClearedOrder()).thenReturn(1L); // head left B
        when(allocated.get(0).getClearedOrder()).thenReturn(2L); // A cleared later, B still clear
        assertFalse(tailHasLeftA(), "tail lost detection in A");
        headIn(3);
        assertFalse(tailHasLeftA(), "still no tail seen, train length unknown");
    }

    @Test
    public void testHeadAndTailLostDetectionFallsBackToLength() {
        buildTransit(500);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        autoTrain(800);
        when(allocated.get(1).getClearedOrder()).thenReturn(1L);
        when(allocated.get(0).getClearedOrder()).thenReturn(2L);
        headIn(2);
        assertFalse(tailHasLeftA(), "only B (500) beyond A, train is 800");
        headIn(3);
        assertTrue(tailHasLeftA(), "B and C (1000) beyond A");
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
