//CarLoadEditFrameTest.java
package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.OperationsSwingTestCase;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations CarLoadEditFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class CarLoadEditFrameTest extends OperationsSwingTestCase {

    public void testCarLoadEditFrame() {
        CarLoadEditFrame f = new CarLoadEditFrame();
        f.initComponents("Boxcar", "");
        f.addTextBox.setText("New Load");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));
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
