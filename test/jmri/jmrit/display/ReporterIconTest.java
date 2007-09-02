package jmri.jmrit.display;

import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.beans.PropertyChangeEvent;

/**
 * Test the ReporterIcon.
 *<P>
 * There is no default (or internal) implementation, so 
 * test via the specific LocoNet implementation
 *
 * Description:
 * @author			Bob Jacobsen  Copyright 2007
 * @version			$Revision: 1.3 $
 */
public class ReporterIconTest extends TestCase {

    ReporterIcon to = null;

	public void testShow() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new ReporterIcon();
        jf.getContentPane().add(to);
        
        // reset instance manager
        jmri.InstanceManager i = new jmri.InstanceManager(){
            protected void init() {
                super.init();
                root = this;
            }
        };
        
        // reset the LocoNet instances, so this behaves independent of 
        // any layout connection
        new jmri.jmrix.loconet.LocoNetInterfaceScaffold();

        // create objects to test
        jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager());
        to.setReporter("1");
        jmri.InstanceManager.reporterManagerInstance().provideReporter("1").setReport("data");

        jf.pack();
        jf.setVisible(true);

	}

	// from here down is testing infrastructure

	public ReporterIconTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", ReporterIconTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(ReporterIconTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	// static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIconTest.class.getName());

}
