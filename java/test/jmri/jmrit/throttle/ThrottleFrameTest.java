package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test simple functioning of ThrottleFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottleFrameTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3);  // allow 3 retries

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

	ThrottleOperator to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));

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

	ThrottleOperator to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));

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

    @Test
    @Ignore("Works locally (Linux) and on Appveyor (Windows).  Unable to find popup after click on Travis")
    public void testToggleMomentaryStatus() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow frame = new ThrottleWindow();
        ThrottleFrame panel = new ThrottleFrame(frame);
        frame.setExtendedState( frame.getExtendedState()|java.awt.Frame.MAXIMIZED_BOTH );
	panel.toFront();

	ThrottleOperator to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));

        to.setAddressValue(new DccLocoAddress(42,false));


        // only check through function 5, since all the buttons
	// are the same class.
        for(int i = 0; i<=5; i++){
           FunctionButton f = to.getFunctionButton(i);
	   Assert.assertTrue("Function F" +i + " continuous",f.getIsLockable());
	   to.toggleFunctionMomentary(i);
           new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
	   Assert.assertFalse("Function F" +i + " momentary",f.getIsLockable());
	}

        to.pushReleaseButton();	
	to.requestClose();
        // the throttle list frame gets created above, but needs to be shown to be disposed
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesList();
        JUnitUtil.disposeFrame(Bundle.getMessage("ThrottleListFrameTile"), true, true);
    }

    @Test
    public void testToggleOnOffStatus() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow frame = new ThrottleWindow();
        ThrottleFrame panel = new ThrottleFrame(frame);
        frame.setExtendedState( frame.getExtendedState()|java.awt.Frame.MAXIMIZED_BOTH );
	panel.toFront();

	ThrottleOperator to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));

        to.setAddressValue(new DccLocoAddress(42,false));

        // only check through function 5, since all the buttons
	// are the same class.
        for(int i = 0; i<=5; i++){
           FunctionButton f = to.getFunctionButton(i);
	   Assert.assertFalse("Function F" +i + " off",f.isSelected());
           JemmyUtil.enterClickAndLeave(f); 
           new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
	   Assert.assertTrue("Function F" +i + " on",f.isSelected());
	}

        to.pushReleaseButton();	
	to.requestClose();
        // the throttle list frame gets created above, but needs to be shown to be disposed
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesList();
        JUnitUtil.disposeFrame(Bundle.getMessage("ThrottleListFrameTile"), true, true);
    }

    @Test
    public void testToggleOnOffStatusAltFunctions() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow frame = new ThrottleWindow();
        ThrottleFrame panel = new ThrottleFrame(frame);
        frame.setExtendedState( frame.getExtendedState()|java.awt.Frame.MAXIMIZED_BOTH );
	panel.toFront();

	ThrottleOperator to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));

        to.setAddressValue(new DccLocoAddress(42,false));

        to.pushAlt1Button();

        // only check functions 20 through 25, since all the buttons
	// are the same class.
        for(int i = 20; i<=25; i++){
           FunctionButton f = to.getFunctionButton(i);
	   Assert.assertFalse("Function F" +i + " off",f.isSelected());
           JemmyUtil.enterClickAndLeave(f); 
           new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
	   Assert.assertTrue("Function F" +i + " on",f.isSelected());
	}

        to.pushReleaseButton();	
	to.requestClose();
        // the throttle list frame gets created above, but needs to be shown to be disposed
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesList();
        JUnitUtil.disposeFrame(Bundle.getMessage("ThrottleListFrameTile"), true, true);
    }

    @Test
    public void testToggleAlt2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow frame = new ThrottleWindow();
        ThrottleFrame panel = new ThrottleFrame(frame);
        frame.setExtendedState( frame.getExtendedState()|java.awt.Frame.MAXIMIZED_BOTH );
	panel.toFront();

	ThrottleOperator to = new ThrottleOperator(Bundle.getMessage("ThrottleTitle"));

        to.setAddressValue(new DccLocoAddress(42,false));

	// the alt2 ("#") button doesn't currently do anything, but
	// we can toggle it to make sure it doesn't throw an exception.
        to.pushAlt1Button();

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
