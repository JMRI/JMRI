package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarEditFrame;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations CarAttributeEditFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class CarAttributeEditFrameTest extends OperationsTestCase {

    @Test
    public void testCarAttributeEditFrameColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.COLOR);
        f.addTextBox.setText("Pink");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new color should appear at start of list
        Assert.assertEquals("new color", "Pink", f.comboBox.getItemAt(0));

        // test replace
        f.comboBox.setSelectedItem("Pink");
        f.addTextBox.setText("Pinker");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        Assert.assertEquals("replaced Pink with Pinker", "Pinker", f.comboBox.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        // black is the first default color
        Assert.assertEquals("old color", "Black", f.comboBox.getItemAt(0));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarAttributeEditFrameKernel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // remove all kernels
        CarManager cm = InstanceManager.getDefault(CarManager.class);
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
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new kernel should appear at start of list after blank
        Assert.assertEquals("new kernel", "TestKernel", f.comboBox.getItemAt(1));

        // test replace
        f.comboBox.setSelectedItem("TestKernel");
        f.addTextBox.setText("TestKernel2");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        Assert.assertEquals("replaced TestKernel with TestKernel2", "TestKernel2", f.comboBox.getItemAt(1));

        // now try and delete
        f.comboBox.setSelectedItem("TestKernel2");
        JemmyUtil.enterClickAndLeave(f.deleteButton);
        // blank is the first default kernel
        Assert.assertEquals("space 2", "", f.comboBox.getItemAt(0));
        Assert.assertEquals("previous kernel 2", "TwoCars", f.comboBox.getItemAt(1));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarAttributeEditFrame2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.LENGTH);
        JUnitUtil.dispose(f);
        f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.OWNER);
        JUnitUtil.dispose(f);
        f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.ROAD);
        JUnitUtil.dispose(f);
        f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.TYPE);
        JUnitUtil.dispose(f);
    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }
}
