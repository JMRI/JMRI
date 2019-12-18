package jmri.jmrit.withrottle;

import java.beans.PropertyChangeEvent;
//import jmri.InstanceManager;
//import jmri.NamedBeanHandleManager;
import jmri.Throttle;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of MultiThrottleController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MultiThrottleControllerTest {
        
   private ControllerInterfaceScaffold cis = null; 
   private ThrottleControllerListenerScaffold tcls = null;
   private MultiThrottleController controller = null;
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", controller);
    }

    @Test
    public void testSetShortAddress(){
       // tests setting the address from the input.  
       // Does not include the prefix.
       Assert.assertTrue("Continue after address",controller.sort("S1"));
       Assert.assertTrue("Address Found",tcls.hasAddressBeenFound());
    }

    @Test
    public void testSetLongAddress(){
       // tests setting the address from the input.  
       // Does not include the prefix.
       Assert.assertTrue("Continue after address",controller.sort("L1234"));
       Assert.assertTrue("Address Found",tcls.hasAddressBeenFound());
    }

    @Test
    public void testSetAndReleaseLongAddress(){
       // set the address
       Assert.assertTrue("Continue after address",controller.sort("L1234"));
       Assert.assertTrue("Address Found",tcls.hasAddressBeenFound());
       // then release it.
       Assert.assertTrue("Continue after release",controller.sort("r"));
       Assert.assertTrue("Address Released",tcls.hasAddressBeenReleased());
    }

    @Test
    public void testSetAndDispatchLongAddress(){
       // set the address
       Assert.assertTrue("Continue after address",controller.sort("L1234"));
       Assert.assertTrue("Address Found",tcls.hasAddressBeenFound());
       // then dispatch it.
       Assert.assertTrue("Continue after release",controller.sort("d"));
       Assert.assertTrue("Address Released",tcls.hasAddressBeenReleased());
    }

    @Test
    public void testSetVelocityChange() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after velocity",controller.sort("V63"));
       Assert.assertEquals("Velocity set",0.5f,t.getSpeedSetting(),0.0005f);
    }

    @Test
    public void testSetEStop() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after EStop",controller.sort("X"));
       Assert.assertTrue("Estop",t.getSpeedSetting()<0.0f);
    }

    @Test
    public void testSetFunction() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       // before notifying the throttle is found, set a function on
       // which runs a couple of additional lines of code in 
       // sendAllFunctionStates.
       t.setF6(true);
       controller.notifyThrottleFound(t);
       // function "on" from withrottle represents a button click event.
       Assert.assertTrue("Continue after set F1 on",controller.sort("F11"));
       Assert.assertTrue("F1 set on",t.getF1());
       Assert.assertTrue("Continue after set F1 off",controller.sort("F11"));
       Assert.assertFalse("F1 set off",t.getF1());
    }

    @Test
    public void testForceFunction() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after set F1 on",controller.sort("f11"));
       Assert.assertTrue("F1 set on",t.getF1());
       Assert.assertTrue("Continue after set F1 off",controller.sort("f01"));
       Assert.assertFalse("F1 set off",t.getF1());
    }

    @Test
    public void testMomentaryFunction() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after set F1 momentary",controller.sort("m11"));
       Assert.assertTrue("F1 set on",t.getF1Momentary());
       Assert.assertTrue("Continue after set F1 continuous",controller.sort("m01"));
       Assert.assertFalse("F1 set off",t.getF1Momentary());
    }

    @Test
    public void testSetDirection() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after set R1",controller.sort("R1"));
       Assert.assertTrue("Velocity set",t.getIsForward());
       Assert.assertTrue("Continue after set R0",controller.sort("R0"));
       Assert.assertFalse("Velocity set",t.getIsForward());
    }

    @Test
    public void testSetIdle() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after velocity",controller.sort("V63"));
       Assert.assertTrue("Continue after Idle",controller.sort("I"));
       Assert.assertEquals("Idle",0.0f,t.getSpeedSetting(),0.0f);
    }

    @Test
    public void testFunctionPropertyChange() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       t.setF1(true);
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>F11",cis.getLastPacket() );
       t.setF1(false);
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>F01",cis.getLastPacket() );
    }

    @Test
    public void testFunctionMomentaryPropertyChange() {
        controller.propertyChange(new PropertyChangeEvent(this,"F1Momentary",false,true));
        Assert.assertNull("WiThrottle Server ignores changes to Momentary Status", cis.getLastPacket() );
        controller.propertyChange(new PropertyChangeEvent(this,"F1Momentary",true,false));
        Assert.assertNull("WiThrottle Server ignores changes to Momentary Status", cis.getLastPacket() );
    }

    @Test
    public void testSpeedStepsPropertyChange() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       t.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_14);
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>s8",cis.getLastPacket() );
       t.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_28);
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>s2",cis.getLastPacket() );
       t.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_128);
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>s1",cis.getLastPacket() );
    }

    @Test
    public void testSpeedPropertyChange() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       t.setSpeedSetting(0.5f);
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>V63",cis.getLastPacket() );
       t.setSpeedSetting(1.0f);
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>V126",cis.getLastPacket() );
       t.setSpeedSetting(0.0f);
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>V0",cis.getLastPacket() );
    }

    @Test
    public void testIsForwardPropertyChange() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       t.setIsForward(false);
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>R0",cis.getLastPacket() );
       t.setIsForward(true);
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>R1",cis.getLastPacket() );
    }

    @Test
    public void testQuitNoAddress(){
       Assert.assertFalse("Stop after quit",controller.sort("Q"));
    }

    @Test
    public void testQuitWithAddress(){
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after velocity",controller.sort("V63"));
       Assert.assertEquals("Velocity set",0.5f,t.getSpeedSetting(),0.0005f);
       Assert.assertFalse("Stop after quit",controller.sort("Q"));
       // current behavior is to set the speed to 0 after quit.
       Assert.assertEquals("Velocity set",0.0f,t.getSpeedSetting(),0.0005f);
    }

    @Test
    public void testVelocityChangeSequence() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null){
           @Override
	   public void setSpeedSetting(float s){
              // override so we can send property changes in sequence.
	   }
       };
       controller.notifyThrottleFound(t);
       cis.reset();
       // withrottle may actually sends more than one speed change when 
       // moving the slider.
       Assert.assertTrue("Continue after velocity",controller.sort("V7"));
       Assert.assertTrue("Continue after velocity",controller.sort("V15"));
       Assert.assertTrue("Continue after velocity",controller.sort("V25"));
       controller.propertyChange(new PropertyChangeEvent(this,Throttle.SPEEDSETTING,0.0f,7.0f/126.0f));
       Assert.assertNull("outgoing message after property change", cis.getLastPacket() );
       Assert.assertTrue("Continue after velocity",controller.sort("V32"));
       Assert.assertTrue("Continue after velocity",controller.sort("V45"));
       Assert.assertTrue("Continue after velocity",controller.sort("V63"));
       controller.propertyChange(new PropertyChangeEvent(this,Throttle.SPEEDSETTING,7.0f/126.0f,15.0f/126.0f));
       Assert.assertNull("outgoing message after property change", cis.getLastPacket() );
       controller.propertyChange(new PropertyChangeEvent(this,Throttle.SPEEDSETTING,15.0f/126.0f,25.0f/126.0f));
       Assert.assertNull("outgoing message after property change", cis.getLastPacket() );
       controller.propertyChange(new PropertyChangeEvent(this,Throttle.SPEEDSETTING,25.0f/126.0f,32.0f/126.0f));
       Assert.assertNull("outgoing message after property change", cis.getLastPacket() );
       controller.propertyChange(new PropertyChangeEvent(this,Throttle.SPEEDSETTING,32.0f/126.0f,45.0f/126.0f));
       Assert.assertNull("outgoing message after property change", cis.getLastPacket() );
       controller.propertyChange(new PropertyChangeEvent(this,Throttle.SPEEDSETTING,0.0f,63.0f/126.0f));
       Assert.assertNull("outgoing message after property change", cis.getLastPacket() );
       controller.propertyChange(new PropertyChangeEvent(this,"SpeedSetting",0.0f,63.0f/126.0f));
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>V63",cis.getLastPacket() );
    }

    @Test
    public void testQueryVelocity() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after query velocity",controller.sort("qV"));
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>V0",cis.getLastPacket() );
    }

    @Test
    public void testQueryDirection() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after query velocity",controller.sort("qR"));
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>R1",cis.getLastPacket() );
    }

    @Test
    public void testQuerySpeedStepMode() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after query velocity",controller.sort("qs"));
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>s1",cis.getLastPacket() );
    }

    @Test
    public void testQueryMomentary() {
       jmri.DccThrottle t = new jmri.jmrix.debugthrottle.DebugThrottle(new jmri.DccLocoAddress(1,false),null);
       controller.notifyThrottleFound(t);
       Assert.assertTrue("Continue after query velocity",controller.sort("qm"));
       Assert.assertEquals("outgoing message after property change", "MAAtest<;>m028",cis.getLastPacket() );
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initRosterConfigManager();
        cis = new ControllerInterfaceScaffold();
        tcls = new ThrottleControllerListenerScaffold();
        controller = new MultiThrottleController('A',"test",tcls,cis);
    }
    
    @After
    public void tearDown() throws Exception {
        cis = null;
        tcls = null;
        controller = null;
        JUnitUtil.tearDown();
    }
}
