package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JFrame;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.sdfeditor.MonitoringLabel class.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class MonitoringLabelTest extends TestCase {

    public void testShowPane() {
        MonitoringLabel p = new MonitoringLabel();
        java.beans.PropertyChangeEvent e
                = new java.beans.PropertyChangeEvent(this, "Event", "old content", "new content");
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.setVisible(true);
        p.propertyChange(e);

        Assert.assertEquals("check content", "new content", p.getText());
    }

    // from here down is testing infrastructure
    public MonitoringLabelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MonitoringLabelTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MonitoringLabelTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
