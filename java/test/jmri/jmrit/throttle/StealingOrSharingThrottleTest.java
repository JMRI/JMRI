package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.DccLocoAddress;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test steal or sharing functionality of ThrottleFrame
 *
 * @author Paul Bender Copyright (C) 2018
 * @author Steve Young Copyright (C) 2019
 */
public class StealingOrSharingThrottleTest {

    private ThrottleWindow frame = null;
    private ThrottleFrame panel = null;
    private ThrottleOperator to = null;

    @Disabled("Jemmy has trouble locating internal frame")
    @Test
    public void testSetAndReleaseWithSteal() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.getQueueTool().waitEmpty(100);  //pause

        // because of the throttle manager we are using, a steal or share
        // request is expected next, and we want to steal.
        Thread add1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("StealShareRequestTitle"), Bundle.getMessage("StealButton"));
        to.pushSetButton();
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "dialog finished");
        to.getQueueTool().waitEmpty(100);  //pause

        Assert.assertEquals("address set", new DccLocoAddress(42, false), to.getAddressValue());
        JUnitAppender.assertErrorMessage("1: Got a steal decision");

        to.pushReleaseButton();
    }

    @Disabled("Jemmy has trouble locating internal frame")
    @Test
    public void testSetAndReleaseWithShare() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.getQueueTool().waitEmpty(100);  //pause

        // because of the throttle manager we are using, a steal or share
        // request is expected next, and we want to share.
        Thread add1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("StealShareRequestTitle"), Bundle.getMessage("ShareButton"));
        to.pushSetButton();
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "dialog finished");
        to.getQueueTool().waitEmpty(100);  //pause


        Assert.assertEquals("address set", new DccLocoAddress(42, false), to.getAddressValue());

        JUnitAppender.assertErrorMessage("1: Got a share decision");
        to.pushReleaseButton();
    }

    @Disabled("Jemmy has trouble locating internal frame")
    @Test
    public void testSetAndRefuseShare() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);

        Thread add1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("StealShareRequestTitle"), Bundle.getMessage("CancelButton"));
        to.pushSetButton();

        // because of the throttle manager we are using, a steal or share
        // request is expected next, and we want to cancel.
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "dialog finished");
        to.getQueueTool().waitEmpty(100);  //pause

        Assert.assertFalse("release button disabled", to.releaseButtonEnabled());
        Assert.assertTrue("set button enabled", to.setButtonEnabled());
    }

    @Disabled("Jemmy has trouble locating internal frame")
    @Test
    public void testRefuseOneShareOne() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.getQueueTool().waitEmpty(100);  //pause

        Thread add1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("StealShareRequestTitle"), Bundle.getMessage("CancelButton"));

        to.pushSetButton();

        // because of the throttle manager we are using, a steal or share
        // request is expected next, and we do not want to steal or share.
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "dialog finished");
        to.getQueueTool().waitEmpty(100);  //pause

        Assert.assertFalse("release button disabled", to.releaseButtonEnabled());
        Assert.assertTrue("set button enabled", to.setButtonEnabled());
        Assert.assertTrue("address field enabled", to.addressFieldEnabled());

       /* Removing bellow test, focus issue on address text field for the bellow typeAddressValue
        * testing for addressFieldEnabled above is already good enough

        to.typeAddressValue(45);
        to.getQueueTool().waitEmpty(100);  //pause

        // because of the throttle manager we are using, a steal or share
        // request is expected next, and we want to share.
        Thread add2 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("StealShareRequestTitle"), Bundle.getMessage("ShareButton"));
        to.pushSetButton();
        JUnitUtil.waitFor(()->{return !(add2.isAlive());}, "dialog 2 finished");
        to.getQueueTool().waitEmpty(100);  //pause

        Assert.assertEquals("address set", new DccLocoAddress(4245, true), to.getAddressValue());
        JUnitAppender.assertErrorMessage("1: Got a share decision");

        to.pushReleaseButton();*/
    }

    @Disabled("Jemmy has trouble locating internal frame")
    @Test
    public void testPreferenceSteal() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // set preference to silent steal
        ThrottlesPreferences tp = jmri.InstanceManager.getDefault(ThrottlesPreferences.class);
        tp.setSilentSteal(true);

        to.typeAddressValue(42);
        to.pushSetButton();
        JUnitAppender.assertErrorMessage("1: Got a steal decision");

    }

    @Disabled("Jemmy has trouble locating internal frame")
    @Test
    public void testPreferenceShare() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // set preference to silent share
        ThrottlesPreferences tp = jmri.InstanceManager.getDefault(ThrottlesPreferences.class);
        tp.setSilentShare(true);

        to.typeAddressValue(42);
        to.pushSetButton();
        JUnitAppender.assertErrorMessage("1: Got a share decision");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        // these tests use the StealingOrSharingThrottleManager.
        jmri.ThrottleManager m = new jmri.managers.StealingOrSharingThrottleManager();
        jmri.InstanceManager.setThrottleManager(m);

        if (!GraphicsEnvironment.isHeadless()) {
            frame = new ThrottleWindow();
            panel = new ThrottleFrame(frame);
            frame.setExtendedState(frame.getExtendedState() | java.awt.Frame.MAXIMIZED_BOTH);
            panel.toFront();
            to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));
        }
    }

    @AfterEach
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
        JUnitUtil.tearDown();
    }
}
