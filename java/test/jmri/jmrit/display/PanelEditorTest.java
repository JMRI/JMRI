package jmri.jmrit.display;

import java.io.File;

import junit.framework.*;

/**
 * PanelEditorTest.java
 *
 * Description:
 * @author			Bob Jacobsen
 * @version			$Revision$
 */
public class PanelEditorTest extends TestCase {

    TurnoutIcon to = null;

	public void testShow() throws Exception {
	    jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager(){
	    };
	    
	    // load and display
	    File f = new File("java"+File.separator+"test"+File.separator+"jmri"+File.separator+"jmrit"+File.separator+"display"+File.separator+"PanelEditorTest1.xml");
        cm.load(f);
        
	}

	public void testShow2() throws Exception {
	    jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager(){
	    };
	    
	    // load and display
	    File f = new File("java/test/jmri/jmrit/display/configurexml/OneOfEach.xml");
        cm.load(f);
        
	}

	public void testShow3() throws Exception {
	    jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager(){
	    };
	    
	    // load and display
	    File f = new File("java/test/jmri/jmrit/display/configurexml/OneOfEach.3.3.3.xml");
        cm.load(f);
        
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
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	// static private Logger log = Logger.getLogger(TurnoutIconTest.class.getName());

}
