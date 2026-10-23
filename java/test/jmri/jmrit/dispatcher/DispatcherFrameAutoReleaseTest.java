package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import jmri.Section;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the final auto-release check of an exited Section,
 * {@link DispatcherFrame#sectionNotRequiredByTrainLength}.
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
        return DispatcherFrame.sectionNotRequiredByTrainLength(true, at, allocated.get(0));
    }

    @Test
    public void testEarlierHoldDecisionIsKept() {
        buildTransit(1000);
        headIn(2);
        for (TrainDetection td : TrainDetection.values()) {
            when(at.getTrainDetection()).thenReturn(td);
            assertFalse(DispatcherFrame.sectionNotRequiredByTrainLength(false, at, allocated.get(0)),
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
    public void testHeadOnlyDispatchedTrainReleasedByLength() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADONLY);
        when(at.getAutoActiveTrain()).thenReturn(null);
        when(at.getMaxTrainLengthMM()).thenReturn(800L);
        headIn(1);
        assertFalse(releaseA(), "head in B, tail may still be in A");
        headIn(2);
        assertTrue(releaseA(), "B is longer than the train");
    }

    @Test
    public void testHeadOnlyDispatchedTrainLongerThanSection() {
        buildTransit(500);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADONLY);
        when(at.getAutoActiveTrain()).thenReturn(null);
        when(at.getMaxTrainLengthMM()).thenReturn(800L);
        headIn(2);
        assertFalse(releaseA(), "only B (500) exited beyond A, train is 800");
        headIn(3);
        assertTrue(releaseA(), "B and C (1000) exited beyond A");
    }

    @Test
    public void testHeadOnlyWithoutLengthIsHeld() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADONLY);
        when(at.getAutoActiveTrain()).thenReturn(null);
        when(at.getMaxTrainLengthMM()).thenReturn(0L);
        when(at.firstNoLengthWarning()).thenReturn(true, false);
        headIn(2);
        assertFalse(releaseA(), "length unknown, keep");
        headIn(3);
        assertFalse(releaseA(), "length unknown, still keep");
        JUnitAppender.assertWarnMessage("Test: has no Max Train Length, Sections behind it will not be released automatically.");
    }

    @Test
    public void testHeadOnlyAutoTrainWithoutLengthIsHeld() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADONLY);
        autoTrain(0);
        headIn(3);
        assertFalse(releaseA());
    }

    @Test
    public void testScriptAutoTrainUsesAutoActiveTrainLength() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADONLY);
        when(at.getMaxTrainLengthMM()).thenReturn(0L);
        autoTrain(800);
        headIn(1);
        assertFalse(releaseA());
        headIn(2);
        assertTrue(releaseA());
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

    /**
     * Also place the detected tail of the train in a Section behind the head.
     * @param index Section index (0 = A)
     */
    private void tailIn(int index) {
        when(sections.get(index).getOccupancy()).thenReturn(Section.OCCUPIED);
    }

    @Test
    public void testHeadAndTailHeldWhileHeadInNextSectionAndTailUndetected() {
        // A has cleared, the head is in B, and the tail has lost detection,
        // so it may still be in A
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        when(at.getAutoActiveTrain()).thenReturn(null);
        when(at.getMaxTrainLengthMM()).thenReturn(800L);
        headIn(1);
        assertFalse(releaseA(), "head in B, nothing beyond A is known to hold the train");
    }

    @Test
    public void testHeadAndTailMeasuredToTheHeadNotTheTail() {
        buildTransit(500);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        when(at.getAutoActiveTrain()).thenReturn(null);
        when(at.getMaxTrainLengthMM()).thenReturn(800L);
        headIn(2);
        tailIn(1);
        assertFalse(releaseA(), "tail in B, head in C: only B (500) beyond A, train is 800");
        headIn(3);
        tailIn(1);
        assertTrue(releaseA(), "tail in B, head in D: B and C (1000) beyond A");
    }

    @Test
    public void testHeadAndTailHeldWhenTailLostDetectionAndNoLength() {
        buildTransit(1000);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        when(at.getAutoActiveTrain()).thenReturn(null);
        when(at.firstNoLengthWarning()).thenReturn(true, false);
        headIn(2);
        assertFalse(releaseA(), "tail lost detection in A");
        headIn(3);
        assertFalse(releaseA(), "still no tail seen, train length unknown");
        JUnitAppender.assertWarnMessage("Test: has no Max Train Length, Sections behind it will not be released automatically.");
    }

    @Test
    public void testHeadAndTailDispatchedLostDetectionReleasedByLength() {
        buildTransit(500);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        when(at.getAutoActiveTrain()).thenReturn(null);
        when(at.getMaxTrainLengthMM()).thenReturn(800L);
        headIn(2);
        assertFalse(releaseA(), "only B (500) beyond A, train is 800");
        headIn(3);
        assertTrue(releaseA(), "B and C (1000) beyond A");
    }

    @Test
    public void testHeadAndTailAutoLostDetectionReleasedByLength() {
        buildTransit(500);
        when(at.getTrainDetection()).thenReturn(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        autoTrain(800);
        headIn(2);
        assertFalse(releaseA(), "only B (500) beyond A, train is 800");
        headIn(3);
        assertTrue(releaseA(), "B and C (1000) beyond A");
    }

    private TrainInfo trainInfo(TrainDetection detection, float lengthScaleMeters) {
        TrainInfo info = new TrainInfo();
        info.setTrainName("T1");
        info.setTrainDetection(detection);
        info.setMaxTrainLengthScaleMeters(lengthScaleMeters);
        return info;
    }

    @Test
    public void testLengthRequiredWhenTailNotAlwaysDetected() {
        assertNotNull(DispatcherFrame.checkTrainLength(trainInfo(TrainDetection.TRAINDETECTION_HEADONLY, 0)));
        assertNotNull(DispatcherFrame.checkTrainLength(trainInfo(TrainDetection.TRAINDETECTION_HEADANDTAIL, 0)));
        assertNull(DispatcherFrame.checkTrainLength(trainInfo(TrainDetection.TRAINDETECTION_HEADONLY, 20)));
        assertNull(DispatcherFrame.checkTrainLength(trainInfo(TrainDetection.TRAINDETECTION_HEADANDTAIL, 20)));
    }

    @Test
    public void testLengthNotRequiredForEntireTrain() {
        assertNull(DispatcherFrame.checkTrainLength(trainInfo(TrainDetection.TRAINDETECTION_WHOLETRAIN, 0)));
    }

    @Test
    public void testLengthErrorNamesTrainAndDetection() {
        String msg = DispatcherFrame.checkTrainLength(trainInfo(TrainDetection.TRAINDETECTION_HEADONLY, 0));
        assertTrue(msg.contains("T1"), msg);
        assertTrue(msg.contains(Bundle.getMessage("TrainDetectionHeadOnly")), msg);
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
