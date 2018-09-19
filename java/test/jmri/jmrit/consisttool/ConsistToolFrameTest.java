package jmri.jmrit.consisttool;

import java.awt.GraphicsEnvironment;
import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of ConsistToolFrame
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class ConsistToolFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolFrame frame = new ConsistToolFrame();
        Assert.assertNotNull("exists", frame );
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testCtorWithCSpossible() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // overwrite the default consist manager set in setUp for this test
        // so that we can check initilization with CSConsists possible.
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager(){
             @Override
             public boolean isCommandStationConsistPossible(){
                 return true;
             }
        });

        ConsistToolFrame frame = new ConsistToolFrame();
        Assert.assertNotNull("exists", frame );
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testAdd() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolFrame frame = new ConsistToolFrame();
	frame.setVisible(true);
        Assert.assertTrue("Consists List empty",InstanceManager.getDefault(ConsistManager.class).getConsistList().isEmpty());
	// get a ConsistToolScaffold
	ConsistToolScaffold cs = new ConsistToolScaffold();
        // set up a consist.
	cs.setConsistAddressValue("1");
	cs.setLocoAddressValue("12");
	cs.pushAddButton();
	// check to see if a conist was added
        Assert.assertFalse("Consists List has one entry",InstanceManager.getDefault(ConsistManager.class).getConsistList().isEmpty());
	//cs.pushDeleteButton();  // is this pressing the right delete button?
        //Assert.assertTrue("Consists List empty after delete",InstanceManager.getDefault(ConsistManager.class).getConsistList().isEmpty());
	cs.requestClose();
    }

    @Test
    @Ignore("need to check if the delete button found is the one at the bottom or the one in the data table")
    public void testAddAndDelete() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolFrame frame = new ConsistToolFrame();
	frame.setVisible(true);
        Assert.assertTrue("Consists List empty",InstanceManager.getDefault(ConsistManager.class).getConsistList().isEmpty());
	// get a ConsistToolScaffold
	ConsistToolScaffold cs = new ConsistToolScaffold();
        // set up a consist.
	cs.setConsistAddressValue("1");
	cs.setLocoAddressValue("12");
	cs.pushAddButton();
	// check to see if a conist was added
        Assert.assertFalse("Consists List has one entry",InstanceManager.getDefault(ConsistManager.class).getConsistList().isEmpty());
	// delee the consist
	cs.pushDeleteButton();  
        Assert.assertTrue("Consists List empty after delete",InstanceManager.getDefault(ConsistManager.class).getConsistList().isEmpty());
	cs.requestClose();
    }

    @Test
    public void testReverseButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolFrame frame = new ConsistToolFrame();
	frame.setVisible(true);
	// get a ConsistToolScaffold
	ConsistToolScaffold cs = new ConsistToolScaffold();
        // set up a consist with two addresses.
	cs.setConsistAddressValue("1");
	cs.setLocoAddressValue("12");
	cs.pushAddButton();
	cs.setLocoAddressValue("13");
	cs.pushAddButton();
	DccLocoAddress conAddr = new DccLocoAddress(1,false);
        Consist c = InstanceManager.getDefault(ConsistManager.class).getConsist(conAddr);
	DccLocoAddress addr12 = new DccLocoAddress(12,false);
	DccLocoAddress addr13 = new DccLocoAddress(13,false);
	Assert.assertEquals("12 position before reverse",jmri.Consist.POSITION_LEAD,c.getPosition(addr12));
	Assert.assertNotEquals("13 position before reverse",jmri.Consist.POSITION_LEAD,c.getPosition(addr13));
        cs.pushReverseButton();
	Assert.assertNotEquals("12 position after reverse",jmri.Consist.POSITION_LEAD,c.getPosition(addr12));
	Assert.assertEquals("13 position after reverse",jmri.Consist.POSITION_LEAD,c.getPosition(addr13));
	cs.requestClose();
    }

    @Test
    public void testThrottle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolFrame frame = new ConsistToolFrame();
	frame.setVisible(true);
	// get a ConsistToolScaffold
	ConsistToolScaffold cs = new ConsistToolScaffold();
        // set up a consist with one addresses.
	cs.setConsistAddressValue("1");
	cs.setLocoAddressValue("12");
	cs.pushAddButton();
        cs.pushThrottleButton();
        JFrameOperator jfo = new JFrameOperator("12(S)");
	// need to verify throttle is setup with two addresses.
	jfo.requestClose();
	cs.requestClose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
	JUnitUtil.initDebugThrottleManager();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


}
