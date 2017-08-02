package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.TestHelper;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Swing jfcUnit tests for the OBlock table
 *
 * @author  Pete Cressman Copyright 2016
 */
public class OBlockTableActionTest extends jmri.util.SwingTestCase {

    public void testInvoke() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        
        // ask for the window to open
        OBlockTableAction a = new OBlockTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));

        // Find new table window by name
        JmriJFrame doc = JmriJFrame.getFrame(jmri.jmrit.beantable.oblock.Bundle.getMessage("TitleOBlocks"));
        Assert.assertNotNull("Occupancy window", doc);
        flushAWT();
        
        javax.swing.JDesktopPane dt = (javax.swing.JDesktopPane)doc.getContentPane();
        javax.swing.JInternalFrame[] fob = dt.getAllFrames();
        Assert.assertNotNull("OBlock window", fob);
        System.out.println();
                
        Assert.assertEquals(4, fob.length);
        flushAWT();
        // Ask to close add window
        TestHelper.disposeWindow(doc, this);
    }

    // from here down is testing infrastructure
    public OBlockTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OBlockTableActionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OBlockTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @Override
    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
