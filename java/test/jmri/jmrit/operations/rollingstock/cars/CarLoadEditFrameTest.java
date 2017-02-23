//CarLoadEditFrameTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsSwingTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the Operations CarLoadEditFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class CarLoadEditFrameTest extends OperationsSwingTestCase {

    public void testCarLoadEditFrame() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        CarLoadEditFrame f = new CarLoadEditFrame();
        f.initComponents("Boxcar", "");
        f.addTextBox.setText("New Load");
        enterClickAndLeave(f.addButton);
        // new load should appear at start of list
        Assert.assertEquals("new load", "New Load", f.loadComboBox.getItemAt(0));

        f.dispose();
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public CarLoadEditFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarLoadEditFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarLoadEditFrameTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
