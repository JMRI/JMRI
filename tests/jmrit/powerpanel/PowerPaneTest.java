/** 
 * JmriTest.java
 *
 * Description:	    tests for the Jmri package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.powerpanel;

//import jmri.jmrit.powerpanel.*;
import jmri.*;

import java.io.*;
import java.beans.PropertyChangeListener;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PowerPaneTest extends TestCase {

	// setup a default PowerManager interface
	public void setUp() {
		manager = new jmri.PowerManager() {
				boolean on = false;
				boolean start = false;
				PropertyChangeListener prop = null;
				
				public void 	setPowerOff() 	throws JmriException { on = false; tell();}
				public void 	setPowerOn()  	throws JmriException { on = true; tell();}
				public boolean 	isPowerOn()  	throws JmriException { return on;}
				public void		setTrackStopped()  throws JmriException { start = false; tell();}
				public void		setTrackStarted()  throws JmriException {start = true; tell();}
				public boolean	isTrackStarted()   throws JmriException { return start;}
				public void dispose() throws JmriException {}
				public void addPropertyChangeListener(PropertyChangeListener p) { prop = p; }
				public void removePropertyChangeListener(PropertyChangeListener p) {}
				void tell() { prop.propertyChange(null);}
			}; // end of anonymous PowerManager class new()
		// store dummy power manager object for retrieval
		InstanceManager.setPowerManager(manager);
	}
	
	// test creation
	public void testCreate() {
		PowerPane p = new PowerPane();
	}

	// test on button routine
	public void testPushOn() {
		PowerPane p = new PowerPane();
		p.onButtonPushed();
		Assert.assertEquals("Testing shown on/off", "On", p.onOffStatus.getText());
	}

	// test off button routine
	public void testPushOff() {
		PowerPane p = new PowerPane();
		p.offButtonPushed();
		Assert.assertEquals("Testing shown on/off", "Off", p.onOffStatus.getText());
	}
	
	// click on button
	public void testOnClicked() {
		PowerPane p = new PowerPane();
		p.onButton.doClick();
		Assert.assertEquals("Testing shown on/off", "On", p.onOffStatus.getText());
	}

	// click off button
	public void testOffClicked() {
		PowerPane p = new PowerPane();
		p.offButton.doClick();
		Assert.assertEquals("Testing shown on/off", "Off", p.onOffStatus.getText());
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
