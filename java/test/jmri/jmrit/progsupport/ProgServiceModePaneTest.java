// ProgServiceModePaneTest.java

package jmri.jmrit.progsupport;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;

import jmri.*;
import jmri.managers.*;

/**
 * Tests for the ProgServiceModePane
 * @author	Bob Jacobsen 2008
 * @version $Revision$
 */
public class ProgServiceModePaneTest extends TestCase {

	public void testCreateHorizontalNone() {
	    // create and show
	    jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("Horizontal None");
	    f.getContentPane().add(
	        new ProgServiceModePane(BoxLayout.X_AXIS));
	    f.pack();
	    f.setLocation(0,0);
	    f.setVisible(true);
	}

	public void testCreateHorizontalDIRECTBYTEMODE() {
	    // add dummy DCC
	    InstanceManager.setProgrammerManager(new DefaultProgrammerManager(
	            (new ProgrammerScaffold(Programmer.DIRECTBYTEMODE))));
	    Assert.assertTrue("programer manager available", InstanceManager.programmerManagerInstance()!=null);
	    // create and show
	    jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("Horizontal DIRECTBYTEMODE");
	    f.getContentPane().add(
	        new ProgServiceModePane(BoxLayout.X_AXIS));
	    f.pack();
	    f.setLocation(0,100);
	    f.setVisible(true);
	}

	public void testCreateVerticalNone() {
	    // create and show
	    jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("Vertical None");
	    f.getContentPane().add(
	        new ProgServiceModePane(BoxLayout.Y_AXIS, 
	                                new ButtonGroup()));
	    f.pack();
	    f.setLocation(0,200);
	    f.setVisible(true);
	}

	// from here down is testing infrastructure

	public ProgServiceModePaneTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {ProgServiceModePaneTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(ProgServiceModePaneTest.class);
		return suite;
	}

    static Logger log = Logger.getLogger(ProgServiceModePaneTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp();
        // clear InstanceManager
        new InstanceManager(){
            protected void init() {
                root = this;
                super.init();
            }
        };
    }
    
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
