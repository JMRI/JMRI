package jmri.util.swing.mdi;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author	Bob Jacobsen Copyright 2003, 2010
 */
public class PackageTest extends TestCase {

    public void testShow() {
        MdiMainFrame f = new MdiMainFrame("Test of MDI Frame",
                "java/test/jmri/util/swing/xml/Gui3LeftTree.xml",
                "java/test/jmri/util/swing/xml/Gui3Menus.xml",
                "java/test/jmri/util/swing/xml/Gui3MainToolBar.xml"
        );
        f.setSize(new java.awt.Dimension(400, 400));
        f.setVisible(true);

        // close
        f.dispose();
    }

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
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
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
