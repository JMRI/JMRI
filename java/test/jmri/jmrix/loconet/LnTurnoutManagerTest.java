// LnTurnoutManagerTest.java

package jmri.jmrix.loconet;

import jmri.Turnout;
import jmri.TurnoutAddress;
import jmri.TurnoutManager;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.LnTurnoutManager class
 * @author			Bob Jacobsen Copyright 2005
 * @version
 */
public class LnTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest  {

	public String getSystemName(int i) {
		return "LT"+i;
	}

	LocoNetInterfaceScaffold lnis;

	public void setUp() {
		// prepare an interface, register
		lnis = new LocoNetInterfaceScaffold();
		// create and register the manager object
		l = new LnTurnoutManager(lnis, lnis, "L", false);
		jmri.InstanceManager.setTurnoutManager(l);
	}

    public void testArraySort() {
        String[] str = new String[]{"8567", "8456"};
        jmri.util.StringUtil.sort(str);
        Assert.assertEquals("first ","8456",str[0]);
    }

	public void testMisses() {
		// sample address object
		TurnoutAddress a = new TurnoutAddress("LT22", "user");
		Assert.assertNotNull("exists", a );

		// try to get nonexistant turnouts
		Assert.assertTrue(null == l.getByUserName("foo"));
		Assert.assertTrue(null == l.getBySystemName("bar"));
	}

	public void testLocoNetMessages() {
		// send messages for 21, 22
		// notify the Ln that somebody else changed it...
		LocoNetMessage m1 = new LocoNetMessage(4);
		m1.setOpCode(0xb1);
		m1.setElement(1, 0x14);     // set CLOSED
		m1.setElement(2, 0x20);
		m1.setElement(3, 0x7b);
		lnis.sendTestMessage(m1);

		// notify the Ln that somebody else changed it...
		LocoNetMessage m2 = new LocoNetMessage(4);
		m2.setOpCode(0xb0);
		m2.setElement(1, 0x15);     // set CLOSED
		m2.setElement(2, 0x20);
		m2.setElement(3, 0x7a);
		lnis.sendTestMessage(m2);


		// try to get turnouts to see if they exist
		Assert.assertTrue(null != l.getBySystemName("LT21"));
		Assert.assertTrue(null != l.getBySystemName("LT22"));

        // check the list
        List<String> testList = new ArrayList<String>(2);
        testList.add("LT21");
        testList.add("LT22");
        Assert.assertEquals("system name list", testList, l.getSystemNameList());
	}

	public void testAsAbstractFactory () {
		// create and register the manager object
		LnTurnoutManager l = new LnTurnoutManager(lnis, lnis, "L", false);
		jmri.InstanceManager.setTurnoutManager(l);

		// ask for a Turnout, and check type
		TurnoutManager t = jmri.InstanceManager.turnoutManagerInstance();

		Turnout o = t.newTurnout("LT21", "my name");


		if (log.isDebugEnabled()) log.debug("received turnout value "+o);
		Assert.assertTrue( null != (LnTurnout)o);

		// make sure loaded into tables
		if (log.isDebugEnabled()) log.debug("by system name: "+t.getBySystemName("LT21"));
		if (log.isDebugEnabled()) log.debug("by user name:   "+t.getByUserName("my name"));

		Assert.assertTrue(null != t.getBySystemName("LT21"));
		Assert.assertTrue(null != t.getByUserName("my name"));

	}


	// from here down is testing infrastructure

	public LnTurnoutManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LnTurnoutManager.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LnTurnoutManagerTest.class);
		return suite;
	}

	 static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LnTurnoutManagerTest.class.getName());

}
