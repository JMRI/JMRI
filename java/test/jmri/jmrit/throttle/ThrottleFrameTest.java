package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ThrottleFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottleFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow frame = new ThrottleWindow();
        ThrottleFrame panel = new ThrottleFrame(frame);
        Assert.assertNotNull("exists", panel);
        JUnitUtil.dispose(frame);
        // the throttle list frame gets created above, but needs to be shown to be disposed
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesList();
        JUnitUtil.disposeFrame(Bundle.getMessage("ThrottleListFrameTile"), true, true);
    }

    @Test
    public void testSetAndReleaseAddress() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow frame = new ThrottleWindow();
        ThrottleFrame panel = new ThrottleFrame(frame);
	panel.toFront();

	//ThrottleOperator to = new ThrottleOperator(panel.getTitle());
	ThrottleOperator to = new ThrottleOperator("Throttle");

        to.setAddressValue(new DccLocoAddress(42,false));

	// the setAddressValue above is currently triggering the set 
	// programatically.  We need a method in the operator which
	// either types the address or picks it from the roster list.
	// once replaces the line above, this comment can be removed
	// and the next line can be uncommented.
        //to.pushSetButton();
        Assert.assertEquals("address set",new DccLocoAddress(42,false),
		 to.getAddressValue());

        to.pushReleaseButton();	
	to.requestClose();
        // the throttle list frame gets created above, but needs to be shown to be disposed
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesList();
        JUnitUtil.disposeFrame(Bundle.getMessage("ThrottleListFrameTile"), true, true);

    }

    @Test
    public void testInitialFunctionStatus() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow frame = new ThrottleWindow();
        ThrottleFrame panel = new ThrottleFrame(frame);
	panel.toFront();

	//ThrottleOperator to = new ThrottleOperator(panel.getTitle());
	ThrottleOperator to = new ThrottleOperator("Throttle");

        to.setAddressValue(new DccLocoAddress(42,false));


        for(int i = 0; i<=28; i++){
           FunctionButton f = to.getFunctionButton(i);
	   Assert.assertFalse("Function F" +i + " off",f.isSelected());
	   Assert.assertTrue("Function F" +i + " continuous",f.getIsLockable());
	}


        to.pushReleaseButton();	
	to.requestClose();
        // the throttle list frame gets created above, but needs to be shown to be disposed
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesList();
        JUnitUtil.disposeFrame(Bundle.getMessage("ThrottleListFrameTile"), true, true);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() {
    	JUnitUtil.tearDown();
    }
}
