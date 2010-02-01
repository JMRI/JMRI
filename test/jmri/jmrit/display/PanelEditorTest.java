package jmri.jmrit.display;

import java.io.File;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * PanelEditorTest.java
 *
 * Description:
 * @author			Bob Jacobsen
 * @version			$Revision: 1.13 $
 */
public class PanelEditorTest extends TestCase {

    TurnoutIcon to = null;

	public void testShow() throws Exception {
	    jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager(){
	    };
	    
	    // load and display
	    File f = new File("java"+File.separator+"test"+File.separator+"jmri"+File.separator+"jmrit"+File.separator+"display"+File.separator+"PanelEditorTest1.xml");
        cm.load(f);
        
        // check some errors were displayed
        jmri.util.JUnitAppender.assertErrorMessage("Turnout 'IT1' not available, icon won't see changes");
        jmri.util.JUnitAppender.assertErrorMessage("Sensor 'IS1' not available, icon won't see changes");
        jmri.util.JUnitAppender.assertErrorMessage("Turnout 'IT1' not available, icon won't see changes");
        jmri.util.JUnitAppender.assertErrorMessage("Sensor 'IS1' not available, icon won't see changes");

	}


	// from here down is testing infrastructure

	public PanelEditorTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", PanelEditorTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(PanelEditorTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	// static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutIconTest.class.getName());

}
