/** 
 * LnPowerManagerTest.java
 *
 * Description:	    tests for the Jmri package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.loconet;

import jmri.*;

import java.io.*;
import java.beans.PropertyChangeListener;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.tests.jmrix.AbstractPowerManagerTest;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnPowerManager;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;

public class LnPowerManagerTest extends AbstractPowerManagerTest {

	// service routines to simulate recieving on, off from interface
	protected void hearOn() {
		LocoNetMessage l = new LocoNetMessage(2);
		l.setOpCode(LnConstants.OPC_GPON);
		controller.sendTestMessage(l);
	}
	
	protected void hearOff() {
		LocoNetMessage l = new LocoNetMessage(2);
		l.setOpCode(LnConstants.OPC_GPOFF);
		controller.sendTestMessage(l);
	}
	
	protected int numListeners() {
		return controller.numListeners();
	}
	
	protected int outboundSize() {
		return controller.outbound.size();
	}
	
	protected boolean outboundOnOK(int index) {
	 return LnConstants.OPC_GPON == 
				((LocoNetMessage)(controller.outbound.elementAt(index))).getOpCode();
	}

	protected boolean outboundOffOK(int index) {
	 return LnConstants.OPC_GPOFF == 
				((LocoNetMessage)(controller.outbound.elementAt(index))).getOpCode();
	}

	// setup a default LnTrafficController interface
	public void setUp() {
		controller = new LocoNetInterfaceScaffold();
		p = new LnPowerManager();
	}
	
	
	LocoNetInterfaceScaffold controller;  // holds dummy LnTrafficController for testing

	// from here down is testing infrastructure
	
	public LnPowerManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LnPowerManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LnPowerManagerTest.class);
		return suite;
	}
	
}
