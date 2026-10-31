package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.Section;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.mockito.InOrder;

/**
 * The Release button in the Allocated Sections table may only release a
 * train's oldest Section once the train has left it, or its furthest
 * allocated Section before the train reaches it, so that the train's route is
 * never left with a gap. The Section directly ahead of the train needs the
 * train to be stopped.
 * <p>
 * The train here has three allocated Sections, allocated in order 1, 2, 3.
 */
@DisabledIfHeadless
public class DispatcherFrameManualReleaseTest {

    private LayoutEditor le;
    private DispatcherFrame d;
    private ActiveTrain train;
    private final List<AllocatedSection> trainSections = new ArrayList<>();

    private AllocatedSection allocated(int number, boolean entered, boolean exited, boolean occupied) {
        Section section = mock(Section.class);
        when(section.getOccupancy()).thenReturn(occupied ? Section.OCCUPIED : Section.UNOCCUPIED);
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(section);
        when(as.getActiveTrain()).thenReturn(train);
        when(as.getSectionName()).thenReturn("Section " + number);
        when(as.getAllocationNumber()).thenReturn(number);
        when(as.getEntered()).thenReturn(entered);
        when(as.getExited()).thenReturn(exited);
        trainSections.add(as);
        return as;
    }

    private void dispatchedTrain(int status) {
        when(train.getMode()).thenReturn(ActiveTrain.DISPATCHED);
        when(train.getStatus()).thenReturn(status);
    }

    @Test
    public void testOldestSectionLeftByTheTrain() {
        dispatchedTrain(ActiveTrain.RUNNING);
        AllocatedSection first = allocated(1, true, true, false);
        allocated(2, true, false, true);
        allocated(3, false, false, false);
        assertNull(d.getManualReleaseRefusal(first), "the train has left its oldest Section");
    }

    @Test
    public void testOldestSectionNotYetLeft() {
        dispatchedTrain(ActiveTrain.RUNNING);
        AllocatedSection first = allocated(1, true, false, false);
        allocated(2, true, false, true);
        allocated(3, false, false, false);
        assertNotNull(d.getManualReleaseRefusal(first), "the train has not left it");
    }

    @Test
    public void testSectionBetweenOthersIsRefused() {
        dispatchedTrain(ActiveTrain.STOPPED);
        allocated(1, true, false, true);
        AllocatedSection middle = allocated(2, false, false, false);
        allocated(3, false, false, false);
        String refusal = d.getManualReleaseRefusal(middle);
        assertNotNull(refusal, "releasing it would leave a gap");
        assertTrue(refusal.contains("Section 2"), refusal);
    }

    @Test
    public void testOccupiedSectionIsRefused() {
        dispatchedTrain(ActiveTrain.STOPPED);
        AllocatedSection first = allocated(1, true, true, true);
        allocated(2, true, false, true);
        assertNotNull(d.getManualReleaseRefusal(first), "it is occupied");
    }

    @Test
    public void testFurthestSectionWhileTheTrainRuns() {
        dispatchedTrain(ActiveTrain.RUNNING);
        allocated(1, true, false, true);
        allocated(2, false, false, false);
        AllocatedSection furthest = allocated(3, false, false, false);
        assertNull(d.getManualReleaseRefusal(furthest), "it is not directly ahead of the train");
    }

    @Test
    public void testSectionAheadNeedsTheTrainStopped() {
        allocated(1, true, false, true);
        AllocatedSection next = allocated(2, false, false, false);

        dispatchedTrain(ActiveTrain.RUNNING);
        assertNotNull(d.getManualReleaseRefusal(next), "running");
        dispatchedTrain(ActiveTrain.STOPPED);
        assertNull(d.getManualReleaseRefusal(next), "stopped");
        dispatchedTrain(ActiveTrain.WAITING);
        assertNull(d.getManualReleaseRefusal(next), "waiting");
    }

    @Test
    public void testSectionAheadOfAnAutomaticTrain() {
        allocated(1, true, false, true);
        AllocatedSection next = allocated(2, false, false, false);
        AutoActiveTrain aat = mock(AutoActiveTrain.class);
        AutoActiveTrain.AutoEngineer engineer = mock(AutoActiveTrain.AutoEngineer.class);
        when(aat.getAutoEngineer()).thenReturn(engineer);
        when(train.getAutoActiveTrain()).thenReturn(aat);
        when(train.getMode()).thenReturn(ActiveTrain.AUTOMATIC);
        when(train.getStatus()).thenReturn(ActiveTrain.WAITING);

        when(engineer.isStopped()).thenReturn(false);
        assertNotNull(d.getManualReleaseRefusal(next), "the engineer has not stopped it, whatever its status");
        when(engineer.isStopped()).thenReturn(true);
        assertNull(d.getManualReleaseRefusal(next), "stopped");
    }

    @Test
    public void testReleaseAheadWithAutoAllocateHoldsTheTrainFirst() {
        dispatchedTrain(ActiveTrain.RUNNING);
        allocated(1, true, false, true);
        allocated(2, false, false, false);
        AllocatedSection furthest = allocated(3, false, false, false);
        d.getAllocatedSectionsList().add(furthest);
        d.setAutoAllocate(true);

        d.releaseAllocatedSectionFromTable(d.getAllocatedSectionsList().indexOf(furthest));

        verify(train, timeout(5000)).removeAllocatedSection(furthest);
        InOrder order = inOrder(train);
        order.verify(train).holdAllocation(true);
        order.verify(train).removeAllocatedSection(furthest);
        assertFalse(d.getAllocatedSectionsList().contains(furthest), "released");
    }

    @Test
    public void testReleaseAheadWithoutAutoAllocateDoesNotHold() {
        dispatchedTrain(ActiveTrain.RUNNING);
        allocated(1, true, false, true);
        allocated(2, false, false, false);
        AllocatedSection furthest = allocated(3, false, false, false);
        d.getAllocatedSectionsList().add(furthest);

        d.releaseAllocatedSectionFromTable(d.getAllocatedSectionsList().indexOf(furthest));

        verify(train).removeAllocatedSection(furthest);
        verify(train, never()).holdAllocation(true);
    }

    @Test
    public void testReleaseOfTheSectionAheadOffersToTerminate() {
        dispatchedTrain(ActiveTrain.STOPPED);
        when(train.getActiveTrainName()).thenReturn("Test Train");
        allocated(1, true, false, true);
        AllocatedSection next = allocated(2, false, false, false);
        d.getAllocatedSectionsList().add(next);

        Thread answer = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"), Bundle.getMessage("ButtonNo"));
        d.releaseAllocatedSectionFromTable(d.getAllocatedSectionsList().indexOf(next));
        JUnitUtil.waitFor(() -> !answer.isAlive(), "terminate question answered");

        verify(train).removeAllocatedSection(next);
        verify(train, never()).terminate();
    }

    @Test
    public void testTerminateAfterReleasingTheSectionAhead() {
        dispatchedTrain(ActiveTrain.STOPPED);
        allocated(1, true, false, true);
        AllocatedSection next = allocated(2, false, false, false);
        d.getAllocatedSectionsList().add(next);

        Thread answer = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"), Bundle.getMessage("ButtonYes"));
        d.releaseAllocatedSectionFromTable(d.getAllocatedSectionsList().indexOf(next));
        JUnitUtil.waitFor(() -> !answer.isAlive(), "terminate question answered");

        verify(train).removeAllocatedSection(next);
        verify(train).terminate();
    }

    @Test
    @Timeout(30) // a wrongly shown question would block the release: fail rather than hang
    public void testFurthestSectionNotAheadOfTheTrainIsNotOfferedTerminate() {
        dispatchedTrain(ActiveTrain.RUNNING);
        allocated(1, true, false, true);
        allocated(2, false, false, false);
        AllocatedSection furthest = allocated(3, false, false, false);
        d.getAllocatedSectionsList().add(furthest);

        d.releaseAllocatedSectionFromTable(d.getAllocatedSectionsList().indexOf(furthest));

        verify(train).removeAllocatedSection(furthest);
        verify(train, never()).terminate();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        le = new LayoutEditor("Manual Release Test");
        d = InstanceManager.getDefault(DispatcherFrame.class);
        train = mock(ActiveTrain.class);
        when(train.getAllocatedSectionList()).thenAnswer(i -> new ArrayList<>(trainSections));
        when(train.getActiveTrainName()).thenReturn("Test Train");
    }

    @AfterEach
    public void tearDown() {
        if (d != null) {
            d.setAutoAllocate(false);
            JUnitUtil.dispose(d);
        }
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
