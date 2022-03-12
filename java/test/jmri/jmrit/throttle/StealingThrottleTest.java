package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test stealing functionality of ThrottleFrame.
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class StealingThrottleTest {


    private ThrottleWindow frame = null;
    private ThrottleFrame panel = null;
    private ThrottleOperator to = null;

    @Disabled("Jemmy has trouble locating internal frame")
    @Test
    public void testSetAndReleaseWithSteal() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));
        to.typeAddressValue(42);
        to.getQueueTool().waitEmpty(100);  //pause

        // because of the throttle manager we are using, a steal
        // request is expected next, and we want to steal.
        Thread add1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("StealRequestTitle"), Bundle.getMessage("ButtonYes"));  // NOI18N
        to.pushSetButton();
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "dialog finished");  // NOI18N
        to.getQueueTool().waitEmpty(100);  //pause

        Assert.assertEquals("address set", new DccLocoAddress(42, false),
                to.getAddressValue());

        to.pushReleaseButton();
    }

    @Disabled("Jemmy has trouble locating internal frame")
    @Test
    public void testSetAndRefuseSteal() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));
        to.typeAddressValue(42);
        to.getQueueTool().waitEmpty(100);  //pause

        // because of the throttle manager we are using, a steal
        // request is expected next, and we do not want to steal.
        Thread add1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("StealRequestTitle"), Bundle.getMessage("ButtonNo"));
        to.pushSetButton();
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "dialog finished");
        to.getQueueTool().waitEmpty(100);  //pause

        Assert.assertFalse("release button disabled", to.releaseButtonEnabled());
        Assert.assertTrue("set button enabled", to.setButtonEnabled());
    }

    @Disabled("Jemmy has trouble locating internal frame")
    @Test
    public void testRefuseOneStealOne() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));
        to.typeAddressValue(42);
        to.getQueueTool().waitEmpty(100);  //pause

        // because of the throttle manager we are using, a steal
        // request is expected next, and we do not want to steal.
        Thread add1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("StealRequestTitle"), Bundle.getMessage("ButtonNo"));
        to.pushSetButton();
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "dialog finished");  // NOI18N
        to.getQueueTool().waitEmpty(100);  //pause

        Assert.assertFalse("release button disabled", to.releaseButtonEnabled());
        Assert.assertTrue("set button enabled", to.setButtonEnabled());
        Assert.assertTrue("address field enabled", to.addressFieldEnabled());

        /* Removing bellow test, focus issue on address text field for the bellow typeAddressValue
         * testing for addressFieldEnabled above is already good enough

        to.typeAddressValue(45);
        to.getQueueTool().waitEmpty(100);  //pause

        // because of the throttle manager we are using, a steal
        // request is expected next, and we want to steal.
        Thread add2 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("StealRequestTitle"), Bundle.getMessage("ButtonYes"));
        to.pushSetButton();
        JUnitUtil.waitFor(()->{return !(add2.isAlive());}, "dialog finished");  // NOI18N
        to.getQueueTool().waitEmpty(100);  //pause

        Assert.assertEquals("address set", new DccLocoAddress(4245, true),
                to.getAddressValue());

        to.pushReleaseButton();*/
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        // these tests use the StealingThrottleManager.
        jmri.ThrottleManager m = new jmri.managers.StealingThrottleManager();
        jmri.InstanceManager.setThrottleManager(m);

        if (!GraphicsEnvironment.isHeadless()) {
            frame = new ThrottleWindow();
            panel = new ThrottleFrame(frame);
            frame.setExtendedState(frame.getExtendedState() | java.awt.Frame.MAXIMIZED_BOTH);
            panel.toFront();
        }
    }

    @AfterEach
    public void tearDown() {
        if (!GraphicsEnvironment.isHeadless()) {
            to.requestClose();
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
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
