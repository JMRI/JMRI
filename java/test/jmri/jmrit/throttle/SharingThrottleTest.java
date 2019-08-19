package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;

/**
 * Test sharing functionality of ThrottleFrame
 *
 * @author Paul Bender Copyright (C) 2018
 * @author Steve Young Copyright (C) 2019
 */
public class SharingThrottleTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3);  // allow 3 retries

    private ThrottleWindow frame = null;
    private ThrottleFrame panel = null;
    private ThrottleOperator to = null;

    @Test
    public void testSetAndReleaseWithShare() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.pushSetButton();

        // because of the throttle manager we are using, a steal
        // request is expected next, and we want to steal.
        to.answerShareQuestion(true); 

        Assert.assertEquals("address set",new DccLocoAddress(42,false),
		                    to.getAddressValue());

        to.pushReleaseButton();	
    }

    @Test
    public void testSetAndRefuseShare() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.pushSetButton();

        // because of the throttle manager we are using, a share
        // request is expected next, and we do not want to share.
        to.answerShareQuestion(false); 
 
        Assert.assertFalse("release button disabled",to.releaseButtonEnabled());
        Assert.assertTrue("set button enabled",to.setButtonEnabled());
    }

    @Test
    public void testRefuseOneShareOne() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        to.typeAddressValue(42);
        to.pushSetButton();

        // because of the throttle manager we are using, a share
        // request is expected next, and we do not want to share.
        to.answerShareQuestion(false); 
 
        Assert.assertFalse("release button disabled",to.releaseButtonEnabled());
        Assert.assertTrue("set button enabled",to.setButtonEnabled());

        to.typeAddressValue(45);
        to.pushSetButton();

        // because of the throttle manager we are using, a share
        // request is expected next, and we want to share.
        to.answerShareQuestion(true); 

        Assert.assertEquals("address set",new DccLocoAddress(4245,true),
		                    to.getAddressValue());

        to.pushReleaseButton();	
    }


    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        // these tests use the SharingThrottleManager.
        jmri.ThrottleManager m = new jmri.managers.SharingThrottleManager();
        jmri.InstanceManager.setThrottleManager(m);
        
	    if(!GraphicsEnvironment.isHeadless()){
           frame = new ThrottleWindow();
           panel = new ThrottleFrame(frame);
           frame.setExtendedState( frame.getExtendedState()|java.awt.Frame.MAXIMIZED_BOTH );
	       panel.toFront();
           to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));
	    }
    }

    @After
    public void tearDown() {
	if(!GraphicsEnvironment.isHeadless()){
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
