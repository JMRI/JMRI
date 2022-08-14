package jmri.jmrit.operations.rollingstock.engines.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations Engines GUI class
 *
 * @author Dan Boudreau Copyright (C) 2010
 *
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class EngineAttributeEditFrameTest extends OperationsTestCase {

    @Test
    public void testEngineAttributeEditFrameModel() {
        JUnitOperationsUtil.initOperationsData();
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(EngineAttributeEditFrame.MODEL);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);
        
        // confirm that the right number of models were loaded
        assertThat(comboBox.getItemCount()).isEqualTo(27);
        // now add a new model name
        addTextBox.setText("New Model");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        jfo.getQueueTool().waitEmpty();
        assertThat(comboBox.getItemAt(12)).withFailMessage("new model name").isEqualTo("New Model");

        // test replace
        comboBox.setSelectedItem("SD45");
        addTextBox.setText("DS54");
        // push replace button
        // need to also push the "Yes" button in the dialog window
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active

        // did the replace work?
        assertThat(comboBox.getItemAt(0)).withFailMessage("replaced SD45 with DS54").isEqualTo("DS54");

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        jfo.getQueueTool().waitEmpty();
        assertThat(comboBox.getItemAt(12)).withFailMessage("new model after delete").isEqualTo("New Model");

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testEngineAttributeEditFrameLength() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(EngineAttributeEditFrame.LENGTH);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        assertThat(comboBox.getItemCount()).isEqualTo(29);
        // now add a new length
        addTextBox.setText("12");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        jfo.getQueueTool().waitEmpty();
        // new length should appear at start of list
        assertThat(comboBox.getItemAt(0)).withFailMessage("new length name").isEqualTo("12");

        // test replace
        comboBox.setSelectedItem("12");
        addTextBox.setText("13");
        // push replace button
        // need to also push the "Yes" button in the dialog window
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active

        // did the replace work?
        assertThat(comboBox.getItemAt(0)).withFailMessage("replaced 12 with 13").isEqualTo("13");

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        assertThat(comboBox.getItemAt(0)).withFailMessage("1st number after delete").isEqualTo("32");

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testEngineAttributeEditFrameLengthInches() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(EngineAttributeEditFrame.LENGTH);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        assertThat(comboBox.getItemCount()).isEqualTo(29);
        // now add a new length in inches
        addTextBox.setText("10" + "\"");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        jfo.getQueueTool().waitEmpty();
        // new length should appear at start of list
        assertThat(comboBox.getItemAt(25)).withFailMessage("new length name").isEqualTo("72");

        // test replace
        comboBox.setSelectedItem("72");
        addTextBox.setText("73");
        // push replace button
        // need to also push the "Yes" button in the dialog window
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active

        // did the replace work?
        assertThat(comboBox.getItemAt(25)).withFailMessage("replaced 72 with 73").isEqualTo("73");

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        jfo.getQueueTool().waitEmpty();
        assertThat(comboBox.getItemAt(0)).withFailMessage("1st number after delete").isEqualTo("32");

        // now try error condition
        addTextBox.setText("A" + "\"");
        // should cause error dialog to appear
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ErrorRsLength"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();

        JUnitUtil.waitFor(()->{return !(t2.isAlive());}, "dialog2 finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testEngineAttributeEditFrameLengthCm() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(EngineAttributeEditFrame.LENGTH);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        assertThat(comboBox.getItemCount()).isEqualTo(29);
        // now add a new length in centimeters
        addTextBox.setText("10" + "cm");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        jfo.getQueueTool().waitEmpty();
        // new length should appear at start of list
        assertThat(comboBox.getItemAt(0)).withFailMessage("new length name").isEqualTo("8");

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        jfo.getQueueTool().waitEmpty();
        assertThat(comboBox.getItemAt(0)).withFailMessage("1st number after delete").isEqualTo("32");

        // now try error condition
        addTextBox.setText("A" + "cm");
        // should cause error dialog to appear
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ErrorRsLength"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testEngineAttributeEditFrameLengthErrors() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(EngineAttributeEditFrame.LENGTH);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        assertThat(comboBox.getItemCount()).isEqualTo(29);
        // now add a bogus length
        addTextBox.setText("A");
        
        Thread t = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[]{Bundle.getMessage("Length")}),
            Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        
        jmri.util.JUnitAppender.assertErrorMessage("length (A) is not an integer");
        assertThat(comboBox.getItemAt(0)).withFailMessage("1st number before bogus add").isEqualTo("32");

        // check for the value "A" 
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            assertThat(comboBox.getItemAt(i)).withFailMessage("check for A").isNotEqualTo("A");
        }

        // now add a negative length
        addTextBox.setText("-1");
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[]{Bundle.getMessage("Length")}),
            Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        JUnitUtil.waitFor(()->{return !(t2.isAlive());}, "dialog2 finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        jmri.util.JUnitAppender.assertErrorMessage("length (-1) has to be a positive number");
        assertThat(comboBox.getItemAt(0)).withFailMessage("1st number before bogus add").isEqualTo("32");

        // check for the value "-1" 
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            assertThat(comboBox.getItemAt(i)).withFailMessage("check for -1").isNotEqualTo("-1");
        }

        // now add a length that is too long
        addTextBox.setText("10000");

        // should cause error dialog to appear
        Thread t3 = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[]{Bundle.getMessage("Length")}),
            Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        JUnitUtil.waitFor(()->{return !(t3.isAlive());}, "dialog3 finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        assertThat(comboBox.getItemAt(0)).withFailMessage("1st number before bogus add").isEqualTo("32");
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testEngineAttributeEditFrameType() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(EngineAttributeEditFrame.TYPE);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        assertThat(comboBox.getItemCount()).isEqualTo(10);
        assertThat(comboBox.getItemAt(0)).withFailMessage("1st type").isEqualTo("Electric");
        // now add a new type
        addTextBox.setText("ABC-TEST_TEST_TEST");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        jfo.getQueueTool().waitEmpty();
        // new type should appear at start of list
        assertThat(comboBox.getItemAt(0)).withFailMessage("new type name").isEqualTo("ABC-TEST_TEST_TEST");

        // test replace
        comboBox.setSelectedItem("ABC-TEST_TEST_TEST");
        addTextBox.setText("ABCDEF-TEST");
        // push replace button
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        // need to also push the "Yes" button in the dialog window
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        // did the replace work?
        assertThat(comboBox.getItemAt(0)).withFailMessage("replaced ABC-TEST").isEqualTo("ABCDEF-TEST");

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        jfo.getQueueTool().waitEmpty();
        assertThat(comboBox.getItemAt(0)).withFailMessage("1st type after delete").isEqualTo("Diesel");

        // enter a type name that is too long
        addTextBox.setText("ABCDEFGHIJKLM-TEST");
        // should cause error dialog to appear
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[]{Bundle.getMessage("Type")}),
            Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        JUnitUtil.waitFor(()->{return !(t2.isAlive());}, "dialog2 finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testEngineAttributeEditFrameRoad() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(EngineAttributeEditFrame.ROAD);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        JTextFieldOperator addTextBox = new JTextFieldOperator(jfo, 0);

        // confirm that the right number of default lengths were loaded
        assertThat(comboBox.getItemCount()).isEqualTo(133);
        assertThat(comboBox.getItemAt(0)).withFailMessage("1st road").isEqualTo("AA");
        // now add a new road
        addTextBox.setText("ABC-TEST");
        new JButtonOperator(jfo,Bundle.getMessage("Add")).push();
        jfo.getQueueTool().waitEmpty();
        assertThat(comboBox.getItemAt(1)).withFailMessage("new road name").isEqualTo("ABC-TEST");
        Assert.assertEquals("Select combobox is correct", "ABC-TEST", comboBox.getSelectedItem());

        // test replace
        addTextBox.setText("ABCDEF-TEST");
        // push replace button
        // need to also push the "Yes" button in the dialog window
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        // did the replace work?
        assertThat(comboBox.getItemAt(1)).withFailMessage("replaced ABC-TEST").isEqualTo("ABCDEF-TEST");

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).push();
        jfo.getQueueTool().waitEmpty();
        assertThat(comboBox.getItemAt(0)).withFailMessage("1st road after delete").isEqualTo("AA");

        // enter a road name that is too long
        addTextBox.setText("ABCDEFGHIJKLM-TEST");
        // should cause error dialog to appear
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotReplace"), new Object[]{Bundle.getMessage("Road")}),
            Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        JUnitUtil.waitFor(()->{return !(t2.isAlive());}, "dialog2 finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active

        // enter a road name that has a reserved character
        addTextBox.setText("A.B");
        // should cause error dialog to appear
        Thread t3 = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("canNotReplace"), new Object[]{Bundle.getMessage("Road")}),
            Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("Replace")).push();
        JUnitUtil.waitFor(()->{return !(t3.isAlive());}, "dialog3 finished");  // NOI18N
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testEngineAttributeEditFrame2() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents(EngineAttributeEditFrame.OWNER);
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);
        jfo.requestClose();
        jfo.waitClosed();

        EngineAttributeEditFrame ff = new EngineAttributeEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            ff.initComponents(EngineAttributeEditFrame.CONSIST);
        });
        JFrameOperator jffo = new JFrameOperator(ff.getTitle());
        Assert.assertNotNull(jffo);
        jffo.requestClose();
        jffo.waitClosed();

    }
}
