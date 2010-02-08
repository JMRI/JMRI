// PackageTest.java

package jmri.util.swing.mdi;

import junit.framework.*;
import javax.swing.*;

import jmri.util.swing.*;

/**
 *
 * @author	    Bob Jacobsen  Copyright 2003, 2010
 * @version         $Revision: 1.1 $
 */
public class PackageTest extends TestCase {

    public void testShow() {
        new MdiMainFrame("test", "apps/demo").setVisible(true);
    }
    
    public void XtestAction() {
        MdiMainFrame m = new MdiMainFrame("test", "apps/decoderpro");
        {
            //JButton b = new JButton(new ButtonTestAction(
            //                          "Open new frame", new jmri.util.swing.sdi.JmriJFrameInterface()));
            //m.getUpperRight().add(b);
        }
        {
            //JButton b = new JButton(new ButtonTestAction(
            //                          "Open in upper panel", new PanedInterface(m)));
            //m.getLowerRight().add(b);
            //
            //b = new JButton(new jmri.jmrit.powerpanel.PowerPanelAction(
            //                          "power", new PanedInterface(m)));
            //m.getLowerRight().add(b);            
        }
        m.setVisible(true);
    }
    
    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class); 

        //suite.addTest(MultiJfcUnitTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
