package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations CarAttributeEditFrame class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class CarAttributeEditFrameTest extends OperationsTestCase {

    @Test
    public void testCarAttributeEditFrameColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.COLOR);
        f.toggleShowQuanity();
        // confirm that the default number of colors is correct
        Assert.assertEquals(12, f.comboBox.getItemCount());
        f.addTextBox.setText("Pink");
        JemmyUtil.enterClickAndLeave(f.addButton);
        Assert.assertEquals("new color", "Pink", f.comboBox.getItemAt(6));

        // test replace
        f.addTextBox.setText("Pinker");
        // push replace button
        JemmyUtil.enterClickAndLeaveThreadSafe(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        // did the replace work?
        Assert.assertEquals("replaced Pink with Pinker", "Pinker", f.comboBox.getItemAt(6));

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
        f.initComponents(CarAttributeEditFrame.KERNEL);
        f.toggleShowQuanity();
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
        JemmyUtil.enterClickAndLeaveThreadSafe(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
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
    public void testCarAttributeEditFrameLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(12, f.comboBox.getItemCount());
        // now add a new length
        f.addTextBox.setText("12");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new length should appear at start of list
        Assert.assertEquals("new length name", "12", f.comboBox.getItemAt(0));

        // test replace
        f.comboBox.setSelectedItem("12");
        f.addTextBox.setText("13");
        // push replace button
        JemmyUtil.enterClickAndLeaveThreadSafe(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        // did the replace work?
        Assert.assertEquals("replaced 12 with 13", "13", f.comboBox.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("1st number after delete", "32", f.comboBox.getItemAt(0));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarAttributeEditFrameLengthInches() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(12, f.comboBox.getItemCount());
        // now add a new length in inches
        f.addTextBox.setText("10" + "\"");
        JemmyUtil.enterClickAndLeave(f.addButton);
        Assert.assertEquals("new length name", "72", f.comboBox.getItemAt(12));

        // test replace
        f.addTextBox.setText("73");
        // push replace button
        JemmyUtil.enterClickAndLeaveThreadSafe(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        // did the replace work?
        Assert.assertEquals("replaced 72 with 73", "73", f.comboBox.getItemAt(12));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("1st number after delete", "32", f.comboBox.getItemAt(0));

        // now try error condition
        f.addTextBox.setText("A" + "\"");
        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);

        JemmyUtil.pressDialogButton(Bundle.getMessage("ErrorRsLength"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarAttributeEditFrameLengthCm() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(12, f.comboBox.getItemCount());
        // now add a new length in centimeters
        f.addTextBox.setText("10" + "cm");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new length should appear at start of list
        Assert.assertEquals("new length name", "8", f.comboBox.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("1st number after delete", "32", f.comboBox.getItemAt(0));

        // now try error condition
        f.addTextBox.setText("A" + "cm");
        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);

        JemmyUtil.pressDialogButton(Bundle.getMessage("ErrorRsLength"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarAttributeEditFrameLengthErrors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(12, f.comboBox.getItemCount());
        // now add a bogus length
        f.addTextBox.setText("A");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);

        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Length") }),
                Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);

        jmri.util.JUnitAppender.assertErrorMessage("length (A) is not an integer");
        Assert.assertEquals("1st number before bogus add", "32", f.comboBox.getItemAt(0));

        // check for the value "A"
        for (int i = 0; i < f.comboBox.getItemCount(); i++) {
            Assert.assertNotEquals("check for A", "A", f.comboBox.getItemAt(i));
        }

        // now add a negative length
        f.addTextBox.setText("-1");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);

        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Length") }),
                Bundle.getMessage("ButtonOK"));

        jmri.util.JUnitAppender.assertErrorMessage("length (-1) has to be a positive number");
        Assert.assertEquals("1st number before bogus add", "32", f.comboBox.getItemAt(0));

        // check for the value "-1"
        for (int i = 0; i < f.comboBox.getItemCount(); i++) {
            Assert.assertNotEquals("check for -1", "-1", f.comboBox.getItemAt(i));
        }

        // now add a length that is too long
        f.addTextBox.setText("10000");

        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);

        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Length") }),
                Bundle.getMessage("ButtonOK"));

        Assert.assertEquals("1st number before bogus add", "32", f.comboBox.getItemAt(0));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarAttributeEditFrameType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.TYPE);
        // confirm that the right number of default types were loaded
        Assert.assertEquals(33, f.comboBox.getItemCount());
        Assert.assertEquals("1st type", "Baggage", f.comboBox.getItemAt(0));

        // now add a new type
        f.addTextBox.setText("ABC-TEST_TEST_TEST");
        // the following should cause two dialog windows to appear
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);

        JemmyUtil.pressDialogButton(Bundle.getMessage("ModifyLocations"), Bundle.getMessage("ButtonNo"));
        JemmyUtil.pressDialogButton(Bundle.getMessage("ModifyTrains"), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);

        // new type should appear at start of list
        Assert.assertEquals("new type name", "ABC-TEST_TEST_TEST", f.comboBox.getItemAt(0));

        // test replace
        f.comboBox.setSelectedItem("ABC-TEST_TEST_TEST");
        f.addTextBox.setText("ABCDEF-TEST");
        // push replace button
        JemmyUtil.enterClickAndLeaveThreadSafe(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        Assert.assertEquals("replaced ABC-TEST", "ABCDEF-TEST", f.comboBox.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("1st type after delete", "Baggage", f.comboBox.getItemAt(0));

        // enter a type name that is too long
        f.addTextBox.setText("ABCDEFGHIJKLM-TEST");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);

        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Type") }),
                Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarAttributeEditFrameTypeError() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.TYPE);
        Assert.assertEquals(33, f.comboBox.getItemCount());

        // can't enter a type name with only spaces
        f.addTextBox.setText("  ");
        Thread add = new Thread(new Runnable() {
            @Override
            public void run() {
                JemmyUtil.enterClickAndLeave(f.addButton);
            }
        });
        add.setName("Add type attribute"); // NOI18N
        add.start();

        try {
            add.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertEquals(33, f.comboBox.getItemCount());

        // now try to add a new type name with the reserved characters
        f.addTextBox.setText("Test & Test");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);
        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Type") }),
                Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        // again try a new type name with the reserved characters
        f.addTextBox.setText("TEST" + CarLoad.SPLIT_CHAR + "TEST");
        // the following should cause dialog window to appear
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);
        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Type") }),
                Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        Assert.assertEquals(33, f.comboBox.getItemCount());
        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarAttributeEditFrameRoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.ROAD);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(133, f.comboBox.getItemCount());
        Assert.assertEquals("1st road", "AA", f.comboBox.getItemAt(0));
        // now add a new road
        f.addTextBox.setText("ABC-TEST");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new road should appear at start of list
        Assert.assertEquals("new road name", "ABC-TEST", f.comboBox.getItemAt(1));

        // test replace
        f.addTextBox.setText("ABCDEF-TEST");
        // push replace button
        JemmyUtil.enterClickAndLeaveThreadSafe(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        // did the replace work?
        Assert.assertEquals("replaced ABC-TEST", "ABCDEF-TEST", f.comboBox.getItemAt(1));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("1st road after delete", "AA", f.comboBox.getItemAt(0));

        // enter a road name that is too long
        f.addTextBox.setText("ABCDEFGHIJKLM-TEST");
        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeaveThreadSafe(f.replaceButton);

        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotReplace"), new Object[] { Bundle.getMessage("Road") }),
                Bundle.getMessage("ButtonOK"));

        // enter a road name that has a reserved character
        f.addTextBox.setText("A.B");
        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeaveThreadSafe(f.replaceButton);

        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotReplace"), new Object[] { Bundle.getMessage("Road") }),
                Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarAttributeEditFrameOwner() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.OWNER);
        f.toggleShowQuanity();
        // check default owner names
        Assert.assertEquals("expected owner names", 2, f.comboBox.getItemCount());
        f.addTextBox.setText("John");
        JemmyUtil.enterClickAndLeave(f.addButton);
        Assert.assertEquals("new owner", "John", f.comboBox.getItemAt(2));

        // test replace
        f.addTextBox.setText("Bob");
        // push replace button
        JemmyUtil.enterClickAndLeaveThreadSafe(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        // did the replace work?
        Assert.assertEquals("replaced John with Bob", "Bob", f.comboBox.getItemAt(1));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("default owner 1", "AT", f.comboBox.getItemAt(0));
        Assert.assertEquals("default owner 2", "DAB", f.comboBox.getItemAt(1));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarAttributeEditFrame2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.OWNER);
        JUnitUtil.dispose(f);
    }
}
