//CarAttributeEditFrameTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.util.List;
import jmri.jmrit.operations.OperationsSwingTestCase;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations CarAttributeEditFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class CarAttributeEditFrameTest extends OperationsSwingTestCase {

    public void testCarAttributeEditFrameColor() {
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.COLOR);
        f.addTextBox.setText("Pink");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));
        // new color should appear at start of list
        Assert.assertEquals("new color", "Pink", f.comboBox.getItemAt(0));

        // test replace
        f.comboBox.setSelectedItem("Pink");
        f.addTextBox.setText("Pinker");
        // push replace button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.replaceButton));
        // need to also push the "Yes" button in the dialog window
        pressDialogButton(f, "Yes");
        // did the replace work?
        Assert.assertEquals("replaced Pink with Pinker", "Pinker", f.comboBox.getItemAt(0));

        getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteButton));
        // black is the first default color
        Assert.assertEquals("old color", "Black", f.comboBox.getItemAt(0));

        f.dispose();
    }

    public void testCarAttributeEditFrameKernel() {
        // remove all kernels
        CarManager cm = CarManager.instance();
        List<String> kList = cm.getKernelNameList();
        for (int i = 0; i < kList.size(); i++) {
            cm.deleteKernel(kList.get(i));
        }
        // create TwoCars kernel
        cm.newKernel("TwoCars");

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.KERNEL);
        // confirm that space and TwoCar kernel exists
        Assert.assertEquals("space 1", "", f.comboBox.getItemAt(0));
        Assert.assertEquals("previous kernel 1", "TwoCars", f.comboBox.getItemAt(1));

        f.addTextBox.setText("TestKernel");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));
        // new kernel should appear at start of list after blank
        Assert.assertEquals("new kernel", "TestKernel", f.comboBox.getItemAt(1));

        // test replace
        f.comboBox.setSelectedItem("TestKernel");
        f.addTextBox.setText("TestKernel2");
        // push replace button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.replaceButton));
        // need to also push the "Yes" button in the dialog window
        pressDialogButton(f, "Yes");
        // did the replace work?
        Assert.assertEquals("replaced TestKernel with TestKernel2", "TestKernel2", f.comboBox.getItemAt(1));

        // now try and delete
        f.comboBox.setSelectedItem("TestKernel2");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteButton));
        // blank is the first default kernel
        Assert.assertEquals("space 2", "", f.comboBox.getItemAt(0));
        Assert.assertEquals("previous kernel 2", "TwoCars", f.comboBox.getItemAt(1));

        f.dispose();
    }

    public void testCarAttributeEditFrame2() {
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.LENGTH);
        f.dispose();
        f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.OWNER);
        f.dispose();
        f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.ROAD);
        f.dispose();
        f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.TYPE);
        f.dispose();
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public CarAttributeEditFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarAttributeEditFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarAttributeEditFrameTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
