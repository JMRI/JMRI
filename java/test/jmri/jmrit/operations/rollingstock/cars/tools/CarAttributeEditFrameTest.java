package jmri.jmrit.operations.rollingstock.cars.tools;

import java.text.MessageFormat;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.KernelManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations CarAttributeEditFrame class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class CarAttributeEditFrameTest extends OperationsTestCase {

    @Test
    public void testCarAttributeEditFrameColor() {

        JUnitOperationsUtil.initOperationsData();
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.COLOR);
            f.toggleShowQuanity();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        // confirm that the default number of colors is correct
        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        Assert.assertEquals(12, comboBox.getItemCount());
        
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);
        addTextBox.setText("Pink");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        Assert.assertEquals("new color", "Pink", comboBox.getItemAt(6));

        // test replace
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        addTextBox.setText("Pinker");
        // push replace button
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        // need to also push the "Yes" button in the dialog window
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        // did the replace work?
        Assert.assertEquals("replaced Pink with Pinker", "Pinker", comboBox.getItemAt(6));

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        // black is the first default color
        Assert.assertEquals("old color", "Black", comboBox.getItemAt(0));

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testCarAttributeEditFrameKernelAdd() {

        // remove all kernels
        KernelManager km = InstanceManager.getDefault(KernelManager.class);
        List<String> kList = km.getNameList();
        for (int i = 0; i < kList.size(); i++) {
            km.deleteKernel(kList.get(i));
        }
        // create TwoCars kernel
        km.newKernel("TwoCars");

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.KERNEL);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that space and TwoCar kernel exists
        Assert.assertEquals("space 1", "", comboBox.getItemAt(0));
        Assert.assertEquals("previous kernel 1", "TwoCars", comboBox.getItemAt(1));

        addTextBox.setText("TestKernel");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        // new kernel should appear at start of list after blank
        Assert.assertEquals("new kernel", "TestKernel", comboBox.getItemAt(1));

        jfo.requestClose();
        jfo.waitClosed();
    }
    
    @Test
    public void testCarAttributeEditFrameKernelReplace() {

        // remove all kernels
        KernelManager km = InstanceManager.getDefault(KernelManager.class);
        List<String> kList = km.getNameList();
        for (int i = 0; i < kList.size(); i++) {
            km.deleteKernel(kList.get(i));
        }
        // create TwoCars kernel
        km.newKernel("TwoCars");

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.KERNEL);
            f.toggleShowQuanity();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // test replace
        comboBox.setSelectedItem("TwoCars");
        addTextBox.setText("TestKernel2");
        // push replace button
        // need to also push the "Yes" button in the dialog window
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        // did the replace work?
        Assert.assertEquals("replaced TwoCars with TestKernel2", "TestKernel2", comboBox.getItemAt(1));

        jfo.requestClose();
        jfo.waitClosed();
    }
    
    @Test
    public void testCarAttributeEditFrameKernelDelete() {

        // remove all kernels
        KernelManager km = InstanceManager.getDefault(KernelManager.class);
        List<String> kList = km.getNameList();
        for (int i = 0; i < kList.size(); i++) {
            km.deleteKernel(kList.get(i));
        }
        // create kernels
        km.newKernel("TwoCars");
        km.newKernel("ThreeCars");

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.KERNEL);
            f.toggleShowQuanity();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);

        // test delete
        comboBox.setSelectedItem("TwoCars");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        jfo.getQueueTool().waitEmpty();
        // blank is the first default kernel
        Assert.assertEquals("space 2", "", comboBox.getItemAt(0));
        Assert.assertEquals("Should be ThreeCars", "ThreeCars", comboBox.getItemAt(1));

        jfo.requestClose();
        jfo.waitClosed();
    }


    @Test
    public void testCarAttributeEditFrameLength() {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.LENGTH);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(12, comboBox.getItemCount());
        // now add a new length
        addTextBox.setText("12");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        // new length should appear at start of list
        Assert.assertEquals("new length name", "12", comboBox.getItemAt(0));

        // test replace
        comboBox.setSelectedItem("12");
        addTextBox.setText("13");
        // push replace button
        // need to also push the "Yes" button in the dialog window
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        // did the replace work?
        Assert.assertEquals("replaced 12 with 13", "13", comboBox.getItemAt(0));

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        Assert.assertEquals("1st number after delete", "32", comboBox.getItemAt(0));

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testCarAttributeEditFrameLengthInches() {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.LENGTH);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);
        jfo.getQueueTool().waitEmpty();

        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(12, comboBox.getItemCount());
        // now add a new length in inches
        addTextBox.setText("10" + "\"");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        jfo.getQueueTool().waitEmpty();
        Assert.assertEquals("new length name", "72", comboBox.getItemAt(12));

        // test replace
        addTextBox.setText("73");
        // push replace button
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        // need to also push the "Yes" button in the dialog window
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        // did the replace work?
        Assert.assertEquals("replaced 72 with 73", "73", comboBox.getItemAt(12));

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        jfo.getQueueTool().waitEmpty();
        Assert.assertEquals("1st number after delete", "32", comboBox.getItemAt(0));

        // now try error condition
        addTextBox.setText("A" + "\"");
        // should cause error dialog to appear
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ErrorRsLength"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();

        JUnitUtil.waitFor(()->{return !(t2.isAlive());}, "dialog2 finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testCarAttributeEditFrameLengthCm() {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.LENGTH);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(12, comboBox.getItemCount());
        // now add a new length in centimeters
        addTextBox.setText("10" + "cm");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        jfo.getQueueTool().waitEmpty();
        // new length should appear at start of list
        Assert.assertEquals("new length name", "8", comboBox.getItemAt(0));

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        jfo.getQueueTool().waitEmpty();
        Assert.assertEquals("1st number after delete", "32", comboBox.getItemAt(0));

        // now try error condition
        addTextBox.setText("A" + "cm");
        // should cause error dialog to appear
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ErrorRsLength"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testCarAttributeEditFrameLengthErrors() {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.LENGTH);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(12, comboBox.getItemCount());
        // now add a bogus length
        addTextBox.setText("A");
        Thread t = JemmyUtil.createModalDialogOperatorThread(
                MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Length") }),
                Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();

        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active

        jmri.util.JUnitAppender.assertErrorMessage("length (A) is not an integer");
        Assert.assertEquals("1st number before bogus add", "32", comboBox.getItemAt(0));

        // check for the value "A"
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Assert.assertNotEquals("check for A", "A", comboBox.getItemAt(i));
        }

        // now add a negative length
        addTextBox.setText("-1");
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(
                MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Length") }),
                Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();

        JUnitUtil.waitFor(()->{return !(t2.isAlive());}, "dialog2 finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        jmri.util.JUnitAppender.assertErrorMessage("length (-1) has to be a positive number");
        Assert.assertEquals("1st number before bogus add", "32", comboBox.getItemAt(0));

        // check for the value "-1"
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Assert.assertNotEquals("check for -1", "-1", comboBox.getItemAt(i));
        }

        // now add a length that is too long
        addTextBox.setText("10000");

        // should cause error dialog to appear
        Thread t3 = JemmyUtil.createModalDialogOperatorThread(
                MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Length") }),
                Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();

        JUnitUtil.waitFor(()->{return !(t3.isAlive());}, "dialog3 finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertEquals("1st number before bogus add", "32", comboBox.getItemAt(0));

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testCarAttributeEditFrameType() {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.TYPE);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default types were loaded
        Assert.assertEquals(33, comboBox.getItemCount());
        Assert.assertEquals("1st type", "Baggage", comboBox.getItemAt(0));

        // now add a new type
        addTextBox.setText("ABC-TEST_TEST_TEST");
        // the following should cause two dialog windows to appear
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ModifyLocations"), Bundle.getMessage("ButtonNo"));
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ModifyTrains"), Bundle.getMessage("ButtonNo"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");
        JUnitUtil.waitFor(()->{return !(t2.isAlive());}, "dialog2 finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        
        // new type should appear at start of list
        Assert.assertEquals("new type name", "ABC-TEST_TEST_TEST", comboBox.getItemAt(0));

        // test replace
        comboBox.setSelectedItem("ABC-TEST_TEST_TEST");
        addTextBox.setText("ABCDEF-TEST");
        // push replace button
        // need to also push the "Yes" button in the dialog window
        Thread t3 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        
        JUnitUtil.waitFor(()->{return !(t3.isAlive());}, "dialog3 finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        // did the replace work?
        Assert.assertEquals("replaced ABC-TEST", "ABCDEF-TEST", comboBox.getItemAt(0));

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        Assert.assertEquals("1st type after delete", "Baggage", comboBox.getItemAt(0));

        // enter a type name that is too long
        addTextBox.setText("ABCDEFGHIJKLM-TEST");
        Thread t4 = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Type") }),
            Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();

        JUnitUtil.waitFor(()->{return !(t4.isAlive());}, "dialog4 finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testCarAttributeEditFrameTypeError() {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.TYPE);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        Assert.assertEquals(33, comboBox.getItemCount());

        // can't enter a type name with only spaces
        addTextBox.setText("  ");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();

        jfo.getQueueTool().waitEmpty();

        Assert.assertEquals(33, comboBox.getItemCount());

        // now try to add a new type name with the reserved characters
        Thread t = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Type") }),
            Bundle.getMessage("ButtonOK"));
        addTextBox.setText("Test & Test");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active

        // again try a new type name with the reserved characters
        addTextBox.setText("TEST" + CarLoad.SPLIT_CHAR + "TEST");
        // the following should cause dialog window to appear
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Type") }),
            Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        JUnitUtil.waitFor(()->{return !(t2.isAlive());}, "dialog2 finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertEquals(33, comboBox.getItemCount());
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testCarAttributeEditFrameRoadReplace() {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.ROAD);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(133, comboBox.getItemCount());

        // test replace
        addTextBox.setText("ABCDEF-TEST");
        // push replace button
        // need to also push the "Yes" button in the dialog window
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        // did the replace work?
        Assert.assertEquals("replaced ABC-TEST", "ABCDEF-TEST", comboBox.getItemAt(0));

        jfo.requestClose();
        jfo.waitClosed();
    }
    
    @Test
    public void testCarAttributeEditFrameRoadDelete() {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.ROAD);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(133, comboBox.getItemCount());

        // test delete
        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        jfo.getQueueTool().waitEmpty();
        Assert.assertEquals("1st road after delete", "ACL", comboBox.getItemAt(0));
        jfo.requestClose();
        jfo.waitClosed();
    }
    
    @Test
    public void testCarAttributeEditFrameRoadAddErrors
    () {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.ROAD);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(133, comboBox.getItemCount());
        
        // enter a road name that is too long
        addTextBox.setText("ABCDEFGHIJKLM-TEST");
        // should cause error dialog to appear
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Road") })
            , Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        
        JUnitUtil.waitFor(()->{return !(t2.isAlive());}, "dialog2 finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active

        // enter a road name that has a reserved character
        addTextBox.setText("A.B");
        // should cause error dialog to appear
        Thread t3 = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[] { Bundle.getMessage("Road") })
            , Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();

        JUnitUtil.waitFor(()->{return !(t3.isAlive());}, "dialog3 finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
    }
    
    @Test
    public void testCarAttributeEditFrameRoadAdd() {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.ROAD);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(133, comboBox.getItemCount());
        Assert.assertEquals("1st road", "AA", comboBox.getItemAt(0));
        // now add a new road
        addTextBox.setText("ABC-TEST");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        jfo.getQueueTool().waitEmpty();
        // new road should appear at start of list
        Assert.assertEquals("new road name", "ABC-TEST", comboBox.getItemAt(1));
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testCarAttributeEditFrameOwner() {

        JUnitOperationsUtil.initOperationsData();
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.OWNER);
            f.toggleShowQuanity();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // check default owner names
        Assert.assertEquals("expected owner names", 2, comboBox.getItemCount());
        addTextBox.setText("John");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        Assert.assertEquals("new owner", "John", comboBox.getItemAt(2));

        // test replace
        addTextBox.setText("Bob");
        // push replace button
        // need to also push the "Yes" button in the dialog window
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();

        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        // did the replace work?
        Assert.assertEquals("replaced John with Bob", "Bob", comboBox.getItemAt(1));

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        jfo.getQueueTool().waitEmpty();
        Assert.assertEquals("default owner 1", "AT", comboBox.getItemAt(0));
        Assert.assertEquals("default owner 2", "DAB", comboBox.getItemAt(1));

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testCarAttributeEditFrame2() {

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarAttributeEditFrame.OWNER);
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(CarAttributeEditFrame.OWNER);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());  // NOI18N
        Assert.assertNotNull(jfo);

        jfo.requestClose();
        jfo.waitClosed();
    }
}
