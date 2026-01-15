package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import jmri.InstanceManager;
import jmri.jmrit.dispatcher.DispatcherFrame.TrainsFrom;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Swing tests for dispatcher options
 *
 * @author Dave Duchamp
 * @author  Paul Bender Copyright(C) 2017
 */
public class DispatcherFrameTest {

    @Test
    @DisabledIfHeadless
    public void testShowAndClose() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // Find new table window by name
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // Ask to close Dispatcher window
        dw.requestClose();
        // we still have a reference to the window, so make sure that clears
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }

    @Test
    @DisabledIfHeadless
    public void testParametersRead() {
        // The Dispatcher functionality is tightly coupled to the Dispatcher
        // Frame.  As a result, we can currently only test seting the
        // options file by creating a DispatcherFrame object.  A future
        // enhancement shold probably break this coupling.

        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // set all options
        d.setLayoutEditor(null);
        d.setUseConnectivity(false);
        d.setTrainsFrom(TrainsFrom.TRAINSFROMROSTER);
        d.setAutoAllocate(false);
        d.setAutoTurnouts(false);
        d.setHasOccupancyDetection(false);
        d.setUseScaleMeters(false);
        d.setShortActiveTrainNames(false);
        d.setShortNameInBlock(true);
        d.setExtraColorForAllocated(false);
        d.setNameInAllocatedBlock(false);
        d.setScale(jmri.ScaleManager.getScale("HO"));
        // test all options
        assertNull( d.getLayoutEditor(), "LayoutEditor");
        assertFalse( d.getUseConnectivity(), "UseConnectivity");
        assertEquals(TrainsFrom.TRAINSFROMROSTER, d.getTrainsFrom());
        assertFalse( d.getAutoAllocate(), "AutoAllocate");
        assertFalse( d.getAutoTurnouts(), "AutoTurnouts");
        assertFalse( d.getHasOccupancyDetection(), "HasOccupancyDetection");
        assertFalse( d.getUseScaleMeters(), "UseScaleMeters");
        assertFalse( d.getShortActiveTrainNames(), "ShortActiveTrainNames");
        assertTrue( d.getShortNameInBlock(), "ShortNameInBlock");
        assertFalse( d.getExtraColorForAllocated(), "ExtraColorForAllocated");
        assertFalse( d.getNameInAllocatedBlock(), "NameInAllocatedBlock");
        assertEquals( jmri.ScaleManager.getScale("HO"), d.getScale(), "Scale");
        // check changing some options
        d.setAutoTurnouts(true);
        assertTrue( d.getAutoTurnouts(), "New AutoTurnouts");
        d.setHasOccupancyDetection(true);
        assertTrue( d.getHasOccupancyDetection(), "New HasOccupancyDetection");
        d.setShortNameInBlock(false);
        assertFalse( d.getShortNameInBlock(), "New ShortNameInBlock");
        d.setScale(jmri.ScaleManager.getScale("N"));
        assertEquals( jmri.ScaleManager.getScale("N"), d.getScale(), "New Scale");

        // Find the window by name and close it.
        (new org.netbeans.jemmy.operators.JFrameOperator(Bundle.getMessage("TitleDispatcher"))).requestClose();
        JUnitUtil.dispose(d);
    }

    @Test
    @DisabledIfHeadless
    public void testAddTrainButton() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // Find new table window by name
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));

        // find the add train Button
        JButtonOperator bo = new JButtonOperator(dw,Bundle.getMessage("InitiateTrain") + "...");

        bo.push();

        // pushing the button should bring up the Add Train frame
        JFrameOperator atf = new JFrameOperator(Bundle.getMessage("AddTrainTitle"));
        // now close the add train frame.
        atf.requestClose();

        // Ask to close Dispatcher window
        dw.requestClose();
        // we still have a reference to the window, so make sure that clears
        JUnitUtil.dispose(d);
    }

    @Test
    @DisabledIfHeadless
    public void testAllocateExtraSectionButton() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // Find new table window by name
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));

        // find the Allocate Extra SectionsButton
        JButtonOperator bo = new JButtonOperator(dw,Bundle.getMessage("AllocateExtra") + "...");

        bo.push();

        // pushing the button should bring up the Extra Sections frame
        JFrameOperator atf = new JFrameOperator(Bundle.getMessage("ExtraTitle"));
        // now close the add train frame.
        atf.requestClose();

        // Ask to close Dispatcher window
        dw.requestClose();
        // we still have a reference to the window, so make sure that clears
        JUnitUtil.dispose(d);
    }

    @Test
    public void testCancelRestartButton() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // Find new table window by name
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));

        // find the Cancel Restart Button
        JButtonOperator bo = new JButtonOperator(dw,Bundle.getMessage("CancelRestart") + "...");

        bo.push();

        // we don't have an active train, so this shouldn't result in any
        // new windows or other results.  This part of the test just verifies
        // we don't have any exceptions.

        // Ask to close Dispatcher window
        dw.requestClose();
        // we still have a reference to the window, so make sure that clears
        JUnitUtil.dispose(d);
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
