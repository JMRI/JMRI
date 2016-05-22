// RouteTableActionTest.java

package jmri.jmrit.beantable;

import org.apache.log4j.Logger;
import javax.swing.JFrame;

import junit.framework.*;



/**
 * Tests for the jmri.jmrit.beantable.RouteTableAction class
 * @author	Bob Jacobsen  Copyright 2004, 2007
 * @version	$Revision$
 */
public class RouteTableActionTest extends TestCase {

    public void testCreate() {
        new RouteTableAction();
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
    }

    public void testInvoke() {
        new RouteTableAction().actionPerformed(null);
    }
    
    public void testX() {
    	JFrame f = jmri.util.JmriJFrame.getFrame("Route Table");
    	Assert.assertTrue("found frame", f !=null );
    	if (f != null)
    		f.dispose();
    }


    // from here down is testing infrastructure

    public RouteTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RouteTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RouteTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(RouteTableActionTest.class.getName());
}
