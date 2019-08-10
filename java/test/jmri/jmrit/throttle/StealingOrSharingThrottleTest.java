package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;

/**
 * Test steal or sharing functionality of ThrottleFrame
 *
 * @author Paul Bender Copyright (C) 2018
 * @author Steve Young Copyright (C) 2019
 */
public class StealingOrSharingThrottleTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3);  // allow 3 retries

    private ThrottleWindow frame = null;
    private ThrottleFrame panel = null;
    private ThrottleOperator to = null;

    @Test
    public void testSetAndReleaseWithSteal() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.pushSetButton();

        // because of the throttle manager we are using, a steal or share
        // request is expected next, and we want to steal.
        to.answerStealShareQuestionSteal();

        Assert.assertEquals("address set", new DccLocoAddress(42, false), to.getAddressValue());
        jmri.util.JUnitAppender.assertErrorMessage("1: Got a steal decision");

        to.pushReleaseButton();
    }

    @Test
    public void testSetAndReleaseWithShare() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.pushSetButton();

        // because of the throttle manager we are using, a steal or share
        // request is expected next, and we want to share.
        to.answerStealShareQuestionShare();

        Assert.assertEquals("address set", new DccLocoAddress(42, false), to.getAddressValue());
        jmri.util.JUnitAppender.assertErrorMessage("1: Got a share decision");

        to.pushReleaseButton();
    }

    @Test
    public void testSetAndRefuseShare() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.pushSetButton();

        // because of the throttle manager we are using, a steal or share
        // request is expected next, and we want to cancel.
        to.answerStealShareQuestionCancel();

        Assert.assertFalse("release button disabled", to.releaseButtonEnabled());
        Assert.assertTrue("set button enabled", to.setButtonEnabled());
    }

    @Test
    public void testRefuseOneShareOne() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.pushSetButton();

        // because of the throttle manager we are using, a steal or share
        // request is expected next, and we do not want to steal or share.
        to.answerStealShareQuestionCancel();

        Assert.assertFalse("release button disabled", to.releaseButtonEnabled());
        Assert.assertTrue("set button enabled", to.setButtonEnabled());

        to.typeAddressValue(45);
        to.pushSetButton();

        // because of the throttle manager we are using, a steal or share
        // request is expected next, and we want to share.
        to.answerStealShareQuestionShare();

        Assert.assertEquals("address set", new DccLocoAddress(4245, true), to.getAddressValue());
        jmri.util.JUnitAppender.assertErrorMessage("1: Got a share decision");

        to.pushReleaseButton();
    }

    @Test
    public void testPreferenceSteal() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // set preference to silent steal
        ThrottlesPreferences tp = jmri.InstanceManager.getDefault(ThrottlesPreferences.class);
        tp.setSilentSteal(true);

        to.typeAddressValue(42);
        to.pushSetButton();
        jmri.util.JUnitAppender.assertErrorMessage("1: Got a steal decision");

    }

    @Test
    public void testPreferenceShare() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // set preference to silent share
        ThrottlesPreferences tp = jmri.InstanceManager.getDefault(ThrottlesPreferences.class);
        tp.setSilentShare(true);

        to.typeAddressValue(42);
        to.pushSetButton();
        jmri.util.JUnitAppender.assertErrorMessage("1: Got a share decision");

    }

    @Before
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
        JUnitUtil.tearDown();
    }
}
