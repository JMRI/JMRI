// RpsMonTest.java

package jmri.jmrix.rps.rpsmon;

import javax.swing.JFrame;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps.rpsmon package.
 * @author      Bob Jacobsen  Copyright 2006
 * @version   $Revision$
 */
public class RpsMonTest extends TestCase {


    // show the window
    public void testDisplay() {
        new RpsMonAction().actionPerformed(null);
    }
    
    public void testFrameCreation() {
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
    	JFrame f = jmri.util.JmriJFrame.getFrame("RPS Monitor");
    	Assert.assertTrue("found frame", f !=null );
    	if (f != null)
    		f.dispose();
    }


    // from here down is testing infrastructure

    public RpsMonTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RpsMonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(RpsMonTest.class);
        return suite;
    }

}
