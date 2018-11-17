package jmri.jmrit.withrottle;

import java.beans.PropertyChangeEvent;
//import jmri.InstanceManager;
//import jmri.NamedBeanHandleManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test stealing behavior of MultiThrottleController
 *
 * @author	Paul Bender Copyright (C) 2018
 */
public class MultiThrottleControllerStealTest {
        
   private ControllerInterfaceScaffold cis = null; 
   private ThrottleControllerListenerScaffold tcls = null;
   private MultiThrottleController controller = null;
    
    @Test
    public void testSetAndReleaseLongAddressWithSteal(){
       // set the address
       Assert.assertTrue("Continue after address",controller.sort("L1234"));
       Assert.assertFalse("Address Found",tcls.hasAddressBeenFound());
       // the throttle manager send a steal request, which triggers a message 
       // from the controller to the device 
       Assert.assertEquals("outgoing message after throttle request", "MAStest<;>test",cis.getLastPacket() );
       // the device then confirms the steal.
       Assert.assertTrue("Continue after confirm steal",controller.sort("L1234"));
       // and the sequence continues as normal.
       Assert.assertTrue("Address Found",tcls.hasAddressBeenFound());
       // then release it.
       Assert.assertTrue("Continue after release",controller.sort("r"));
       Assert.assertTrue("Address Released",tcls.hasAddressBeenReleased());
       Assert.assertEquals("outgoing message after release", "MA-test<;>",cis.getLastPacket() );
    }

    @Test
    public void testRefuseOneStealOne() {
       // set the address
       Assert.assertTrue("Continue after address",controller.sort("L1234"));
       Assert.assertFalse("Address Found",tcls.hasAddressBeenFound());
       // the throttle manager send a steal request, which triggers a message 
       // from the controller to the device 
       Assert.assertEquals("outgoing message after throttle request", "MAStest<;>test",cis.getLastPacket() );
       // to refuse the steal, we have to send a different address
       Assert.assertTrue("Continue after address",controller.sort("L4321"));
       Assert.assertFalse("Address Found",tcls.hasAddressBeenFound());
       // from the controller to the device 
       Assert.assertEquals("outgoing message after throttle request", "MAStest<;>test",cis.getLastPacket() );

       // to refuse the steal, we have to send a different address
       // the device then confirms the steal.
       Assert.assertTrue("Continue after confirm steal",controller.sort("L4321"));
       // and the sequence continues as normal.
       Assert.assertTrue("Address Found",tcls.hasAddressBeenFound());
       // then release it.
       Assert.assertTrue("Continue after release",controller.sort("r"));
       Assert.assertTrue("Address Released",tcls.hasAddressBeenReleased());
       Assert.assertEquals("outgoing message after release", "MA-test<;>",cis.getLastPacket() );
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // these tests use the StealingThrottleManager.
        jmri.ThrottleManager m = new jmri.managers.StealingThrottleManager();
        jmri.InstanceManager.setThrottleManager(m);
        cis = new ControllerInterfaceScaffold();
        tcls = new ThrottleControllerListenerScaffold();
        controller = new MultiThrottleController('A',"test",tcls,cis);
    }
    
    @After
    public void tearDown() {
        cis = null;
        tcls = null;
        controller = null;
        JUnitUtil.tearDown();
    }
}
