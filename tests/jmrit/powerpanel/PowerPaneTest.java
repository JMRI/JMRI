/** 
 * JmriTest.java
 *
 * Description:	    tests for the Jmri package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrit.powerpanel;

import jmri.jmrit.powerpanel.*;
import jmri.*;

import java.io.*;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PowerPaneTest extends TestCase {

	// setup a default PowerManager interface
	public void setUp() {
		manager = new jmri.PowerManager() {
				public String s = "";
				public void 	setPowerOff() 	throws JmriException {}
				public void 	setPowerOn()  	throws JmriException {}
				public boolean 	isPowerOn()  	throws JmriException { return false;}
	
				public void		setTrackStopped()  throws JmriException {}
				public void		setTrackStarted()  throws JmriException {}
				public boolean	isTrackStarted()   throws JmriException { return false;}

				// to free resources when no longer used
				public void dispose() throws JmriException {}
			}; // end of anonymous PowerManager class new()
		// store dummy power manager object for retrieval
		InstanceManager.setPowerManager(manager);
	}
	
	// test creation
	public void testCreate() {
		PowerPane p = new PowerPane();
	}

	// push on button
	public void testPushOn() {
		PowerPane p = new PowerPane();
		p.onButtonPushed();
		Assert.assertEquals("Testing shown on/off", "On", p.shownOnOffState());
	}

	// push off button
	public void testPushOff() {
		PowerPane p = new PowerPane();
		p.offButtonPushed();
		Assert.assertEquals("Testing shown on/off", "Off", p.shownOnOffState());
	}
	
	
	jmri.PowerManager manager;  // holds dummy for testing

	// from here down is testing infrastructure
	
	public PowerPaneTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {PowerPaneTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(PowerPaneTest.class);
		return suite;
	}
	
}
