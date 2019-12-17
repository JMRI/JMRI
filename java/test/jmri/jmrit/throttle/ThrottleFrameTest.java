package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import java.io.File;
import jmri.InstanceManager;
import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

/**
 * Test simple functioning of ThrottleFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottleFrameTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3);  // allow 3 retries

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ThrottleWindow frame = null;
    private ThrottleFrame panel = null;
    private ThrottleOperator to = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", panel);
    }

    @Test
    public void testSetAndReleaseAddress() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.pushSetButton();
        Assert.assertEquals("address set", new DccLocoAddress(42, false),
                to.getAddressValue());

        to.pushReleaseButton();
    }

    @Test
    public void testSetWithoutRelease() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));

        Assert.assertEquals("address set", new DccLocoAddress(42, false),
                to.getAddressValue());
        // don't release the throttle here.  When the frame is disposed,
        // the throttle will still be attached, which causes a different code
        // path to be executed. 
    }

    @Test
    public void testInitialFunctionStatus() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));

        for (int i = 0; i <= 28; i++) {
            FunctionButton f = to.getFunctionButton(i);
            Assert.assertFalse("Function F" + i + " off", f.isSelected());
            Assert.assertTrue("Function F" + i + " continuous", f.getIsLockable());
        }

        to.pushReleaseButton();
    }

    @Test
    @Ignore("Works locally (Linux) and on Appveyor (Windows).  Unable to find popup after click on Travis")
    public void testToggleMomentaryStatus() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));

        // only check through function 5, since all the buttons
        // are the same class.
        for (int i = 0; i <= 5; i++) {
            FunctionButton f = to.getFunctionButton(i);
            Assert.assertTrue("Function F" + i + " continuous", f.getIsLockable());
            to.toggleFunctionMomentary(i);
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
            Assert.assertFalse("Function F" + i + " momentary", f.getIsLockable());
        }

        to.pushReleaseButton();
    }

    @Test
    public void testToggleOnOffStatus() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setExtendedState(frame.getExtendedState() | java.awt.Frame.MAXIMIZED_BOTH);
        panel.toFront();

        to.setAddressValue(new DccLocoAddress(42, false));

        // only check through function 5, since all the buttons
        // are the same class.
        for (int i = 0; i <= 5; i++) {
            FunctionButton f = to.getFunctionButton(i);
            Assert.assertFalse("Function F" + i + " off", f.isSelected());
            JemmyUtil.enterClickAndLeave(f);
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
            Assert.assertTrue("Function F" + i + " on", f.isSelected());
        }

        to.pushReleaseButton();
    }

    @Test
    public void testToggleOnOffStatusAltFunctions() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));

        to.pushAlt1Button();

        // only check functions 20 through 25, since all the buttons
        // are the same class.
        for (int i = 20; i <= 25; i++) {
            FunctionButton f = to.getFunctionButton(i);
            Assert.assertFalse("Function F" + i + " off", f.isSelected());
            JemmyUtil.enterClickAndLeave(f);
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
            Assert.assertTrue("Function F" + i + " on", f.isSelected());
        }

        to.pushReleaseButton();
    }

    @Test
    public void testToggleAlt2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));

        // the alt2 ("#") button doesn't currently do anything, but
        // we can toggle it to make sure it doesn't throw an exception.
        to.pushAlt1Button();

        to.pushReleaseButton();
    }

    // Tests for Control (Speed and Direction) panel.
    @Test
    public void testStopButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));
        to.setSpeedSlider(28);

        to.pushStopButton();
        // should verify the throttle is set to stop.
        Assert.assertEquals("Speed set to Stop", 0, to.getSpeedSliderValue());
        Assert.assertTrue("Throttle Speed Stop", to.getAttachedThrottle().getSpeedSetting() < 0);

        to.pushReleaseButton();
    }

    @Test
    public void testEStopButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setExtendedState(frame.getExtendedState() | java.awt.Frame.MAXIMIZED_BOTH);
        panel.toFront();

        to.setAddressValue(new DccLocoAddress(42, false));
        to.setSpeedSlider(28);

        to.pushEStopButton();
        // should verify the throttle is set to stop.
        Assert.assertEquals("Speed set to EStop", 0, to.getSpeedSliderValue());
        Assert.assertTrue("Throttle Speed EStop", to.getAttachedThrottle().getSpeedSetting() < 0);

        to.pushReleaseButton();
    }

    @Test
    public void testIdleButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));
        to.setSpeedSlider(28);

        to.pushIdleButton();
        // should verify the throttle is set to Idle.
        Assert.assertEquals("Speed set to Stop", 0, to.getSpeedSliderValue());
        Assert.assertEquals("Throttle Speed Idle", 0.0, to.getAttachedThrottle().getSpeedSetting(), 0.005);

        to.pushReleaseButton();
    }

    @Test
    public void testSliderMaximumSpeed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));

        to.speedSliderMaximum();

        Assert.assertEquals("Speed set to Maximum", 126, to.getSpeedSliderValue());
        Assert.assertEquals("Throttle Speed Maximum", 1.0, to.getAttachedThrottle().getSpeedSetting(), 0.005);

        to.pushReleaseButton();
    }

    @Test
    public void testSliderMinimumSpeed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));

        to.setSpeedSlider(28);
        to.slideSpeedSlider(1); // jemmy can't slide the slider to zero for some
        // reason.
        Assert.assertEquals("Speed set to Minimum", 1, to.getSpeedSliderValue());

        to.pushReleaseButton();
    }

    @Test
    public void testForwardButtonPress() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));

        to.pushForwardButton();	// need to verify this took effect.	
        Assert.assertTrue("Forward Direction", to.getAttachedThrottle().getIsForward());

        to.pushReleaseButton();
    }

    @Test
    public void testReverseButtonPress() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));

        to.pushReverseButton(); // need to verify this took effect.	
        Assert.assertFalse("Reverse Direction", to.getAttachedThrottle().getIsForward());
        to.pushReleaseButton();
    }

    @Test
    public void testDirectionChangeWhileMoving() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));
        to.setSpeedSlider(28);

        Assert.assertEquals("Speed setting 28", 28, to.getSpeedSliderValue());
        float speed = to.getAttachedThrottle().getSpeedSetting();

        to.pushForwardButton();	// need to verify this took effect.	
        Assert.assertTrue("Forward Direction", to.getAttachedThrottle().getIsForward());
        // and the absolute value of the speed is the same.

        Assert.assertEquals("Throttle Speed Setting after forward", Math.abs(speed), Math.abs(to.getAttachedThrottle().getSpeedSetting()), 0.0);

        to.pushReverseButton();	// need to verify this took effect.	
        Assert.assertFalse("Reverse Direction", to.getAttachedThrottle().getIsForward());
        // and the absolute value of the speed is the same.

        Assert.assertEquals("Throttle Speed Setting after reverse", Math.abs(speed), Math.abs(to.getAttachedThrottle().getSpeedSetting()), 0.0);

        to.pushReleaseButton();
    }

    @Test
    public void testChangeToSpeedStepMode() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));

        to.setSpeedStepDisplay();

        to.pushReleaseButton();
    }

    @Test
    public void testSpinnerMaximumSpeed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));
        to.setSpeedStepDisplay();

        to.speedSpinnerMaximum();

        Assert.assertEquals("Throttle Speed Maximum", 1.0, to.getAttachedThrottle().getSpeedSetting(), 0.005);

        to.pushReleaseButton();
    }

    @Test
    public void testSpinnerMinimumSpeed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.setAddressValue(new DccLocoAddress(42, false));
        to.setSpeedStepDisplay();

        to.setSpeedSpinner(28);
        to.speedSpinnerMinimum();
        Assert.assertEquals("Throttle Speed Minimum", 0.0, to.getAttachedThrottle().getSpeedSetting(), 0.005);

        to.pushReleaseButton();
    }

    @Test
    public void testSetAndGetFileName() throws java.io.IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        String fileName = folder.newFolder().getPath() + File.separator + "testThrotttle.xml";
        panel.setLastUsedSaveFile(fileName);
        Assert.assertEquals("filename after set", fileName, panel.getLastUsedSaveFile());
    }

    @Test
    public void testSaveThrottle() throws java.io.IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        String fileName = folder.newFolder().getPath() + File.separator + "testThrotttle.xml";
        panel.setLastUsedSaveFile(fileName);
        // right now, just verify no error
        panel.saveThrottle();
    }

    @Test
    public void testDispatchReleaseButtonPropertyChangeListener() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertFalse("Release button NOT enabled as no loco", to.releaseButtonEnabled());
        Assert.assertFalse("Dispatch button NOT enabled as no loco", to.dispatchButtonEnabled());

        to.setAddressValue(new DccLocoAddress(1234, true));

        Assert.assertTrue("Release button enabled", to.releaseButtonEnabled());

        to.getAttachedThrottle().notifyThrottleReleaseEnabled(true);
        to.getAttachedThrottle().notifyThrottleDispatchEnabled(true);

        Assert.assertTrue("Dispatch button enabled", to.dispatchButtonEnabled());
        Assert.assertTrue("Release button enabled", to.releaseButtonEnabled());

        to.getAttachedThrottle().notifyThrottleDispatchEnabled(false);
        to.getAttachedThrottle().notifyThrottleReleaseEnabled(false);

        Assert.assertFalse("Release button NOT enabled", to.releaseButtonEnabled());
        Assert.assertFalse("Dispatch button NOT enabled", to.dispatchButtonEnabled());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();

        if (!GraphicsEnvironment.isHeadless()) {
            frame = new ThrottleWindow();
            panel = new ThrottleFrame(frame);
            frame.setExtendedState(frame.getExtendedState() | java.awt.Frame.MAXIMIZED_BOTH);
            panel.toFront();
            to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));
        }
    }

    @After
    public void tearDown() {
        if (!GraphicsEnvironment.isHeadless()) {
            to.requestClose();
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame to close
            JUnitUtil.dispose(frame);
            // the throttle list frame gets created above, but needs to be shown to be disposed
            InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesList();
            JUnitUtil.disposeFrame(Bundle.getMessage("ThrottleListFrameTile"), true, true);
        }
        panel = null;
        frame = null;
        to = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
