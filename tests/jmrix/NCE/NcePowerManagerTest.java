/** 
 * NcePowerManagerTest.java
 *
 * Description:	    tests for the Jmri package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.nce;

import jmri.*;

import java.io.*;
import java.beans.PropertyChangeListener;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.NcePowerManager;

public class NcePowerManagerTest extends TestCase {

	// setup a default LnTrafficController interface
	public void setUp() {
	}
	
	// test creation
	public void testCreate() {
		NcePowerManager p = new NcePowerManager();
	}

	// test power
	public void testSetPowerOn() {
		NcePowerManager p = new NcePowerManager();
		p.setPower(PowerManager.ON);
		// check one message sent, correct form
		//Assert.assertEquals("messages sent", 1, controller.outbound.size());
		//Assert.assertEquals("message type", LnConstants.OPC_GPON, 
		//					((LocoNetMessage)(controller.outbound.elementAt(0))).getOpCode());
		
	}

	public void testDispose() throws JmriException {
		NcePowerManager p = new NcePowerManager();
		p.setPower(PowerManager.ON); // in case registration is deferred
		p.getPower();
		p.dispose();
	}

	static private boolean listenerResult = false;
	private class Listen implements PropertyChangeListener {
		public void propertyChange(java.beans.PropertyChangeEvent e) {listenerResult = true;}
	}
		
	public void testAddListener() {
		NcePowerManager p = new NcePowerManager();
		p.addPropertyChangeListener(new Listen());
		// receive a message setting the state
		listenerResult = false;
	}
	
	public void testRemoveListener() {
		NcePowerManager p = new NcePowerManager();
		p.addPropertyChangeListener(new Listen());
		p.removePropertyChangeListener(new Listen());
		listenerResult = false;
		//l = new LocoNetMessage(2);
		//l.setOpCode(LnConstants.OPC_GPON);
		//controller.sendTestMessage(l);
		Assert.assertTrue("listener invoked by GPON", listenerResult);
	}

	// from here down is testing infrastructure
	
	public NcePowerManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {NcePowerManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(NcePowerManagerTest.class);
		return suite;
	}
	
}
