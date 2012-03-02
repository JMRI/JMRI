// JythonTest.java

package jmri.jmrit.jython;

import javax.swing.JFrame;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.jmrit.jython tree
 *
 * Some of these tests are here, as they're cross-class functions
 *
 * @author	    Bob Jacobsen  Copyright 2009
 * @version         $Revision$
 */
public class JythonTest extends TestCase {

    // Really a check of Jython init, including the defaults file
    public void testExec() {
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        jmri.util.JUnitAppender.clearBacklog();
        // open output window
        try {
            new JythonWindow().actionPerformed(null);
        } catch (Exception e ) {
            Assert.fail("exception opening output window: "+e);
        }
        
        // create input window
        InputWindow w = new InputWindow();
        
        // run a null test
        try {
            w.buttonPressed();
        } catch (Exception e ) {
            Assert.fail("exception during execution: "+e);
        }
        
        // error messages are a fail
        if (jmri.util.JUnitAppender.clearBacklog() != 0) {
            Assert.fail("Emitted error messages caused test to fail");
        }
    }
    
    public void testInput() {
        new InputWindowAction().actionPerformed(null);
    }
    
    public void testXXFrameCreation() {
    	JFrame f = jmri.util.JmriJFrame.getFrame("Script Entry");
    	Assert.assertTrue("found frame", f !=null );
    	if (f != null)
    		f.dispose();
    	f = jmri.util.JmriJFrame.getFrame("Script Output");
    	Assert.assertTrue("found frame", f !=null );
    	if (f != null)
    		f.dispose();
    }
    
    // from here down is testing infrastructure
    public JythonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JythonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JythonTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
