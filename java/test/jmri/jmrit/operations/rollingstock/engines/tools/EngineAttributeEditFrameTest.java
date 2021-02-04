package jmri.jmrit.operations.rollingstock.engines.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations Engines GUI class
 *
 * @author Dan Boudreau Copyright (C) 2010
 *
 */
public class EngineAttributeEditFrameTest extends OperationsTestCase {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEngineAttributeEditFrameModel() {
        JUnitOperationsUtil.initOperationsData();
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.MODEL);
        // confirm that the right number of models were loaded
        assertThat(f.comboBox.getItemCount()).isEqualTo(27);
        // now add a new model name
        f.addTextBox.setText("New Model");
        JemmyUtil.enterClickAndLeave(f.addButton);
        assertThat(f.comboBox.getItemAt(12)).withFailMessage("new model name").isEqualTo("New Model");

        // test replace
        f.comboBox.setSelectedItem("SD45");
        f.addTextBox.setText("DS54");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("replaced SD45 with DS54").isEqualTo("DS54");

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        assertThat(f.comboBox.getItemAt(12)).withFailMessage("new model after delete").isEqualTo("New Model");

        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEngineAttributeEditFrameLength() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        assertThat(f.comboBox.getItemCount()).isEqualTo(29);
        // now add a new length
        f.addTextBox.setText("12");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new length should appear at start of list
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("new length name").isEqualTo("12");

        // test replace
        f.comboBox.setSelectedItem("12");
        f.addTextBox.setText("13");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("replaced 12 with 13").isEqualTo("13");

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("1st number after delete").isEqualTo("32");

        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEngineAttributeEditFrameLengthInches() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        assertThat(f.comboBox.getItemCount()).isEqualTo(29);
        // now add a new length in inches
        f.addTextBox.setText("10" + "\"");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new length should appear at start of list
        assertThat(f.comboBox.getItemAt(25)).withFailMessage("new length name").isEqualTo("72");

        // test replace
        f.comboBox.setSelectedItem("72");
        f.addTextBox.setText("73");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        assertThat(f.comboBox.getItemAt(25)).withFailMessage("replaced 72 with 73").isEqualTo("73");

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("1st number after delete").isEqualTo("32");

        // now try error condition
        f.addTextBox.setText("A" + "\"");
        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeave(f.addButton);

        JemmyUtil.pressDialogButton(Bundle.getMessage("ErrorRsLength"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEngineAttributeEditFrameLengthCm() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        assertThat(f.comboBox.getItemCount()).isEqualTo(29);
        // now add a new length in centimeters
        f.addTextBox.setText("10" + "cm");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new length should appear at start of list
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("new length name").isEqualTo("8");

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("1st number after delete").isEqualTo("32");

        // now try error condition
        f.addTextBox.setText("A" + "cm");
        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeave(f.addButton);

        JemmyUtil.pressDialogButton(Bundle.getMessage("ErrorRsLength"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEngineAttributeEditFrameLengthErrors() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        assertThat(f.comboBox.getItemCount()).isEqualTo(29);
        // now add a bogus length
        f.addTextBox.setText("A");
        JemmyUtil.enterClickAndLeave(f.addButton);
        
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle
                .getMessage("canNotAdd"), new Object[]{Bundle.getMessage("Length")}), Bundle.getMessage("ButtonOK"));

        jmri.util.JUnitAppender.assertErrorMessage("length (A) is not an integer");
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("1st number before bogus add").isEqualTo("32");

        // check for the value "A" 
        for (int i = 0; i < f.comboBox.getItemCount(); i++) {
            assertThat(f.comboBox.getItemAt(i)).withFailMessage("check for A").isNotEqualTo("A");
        }

        // now add a negative length
        f.addTextBox.setText("-1");
        JemmyUtil.enterClickAndLeave(f.addButton);
        
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle
                .getMessage("canNotAdd"), new Object[]{Bundle.getMessage("Length")}), Bundle.getMessage("ButtonOK"));

        jmri.util.JUnitAppender.assertErrorMessage("length (-1) has to be a positive number");
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("1st number before bogus add").isEqualTo("32");

        // check for the value "-1" 
        for (int i = 0; i < f.comboBox.getItemCount(); i++) {
            assertThat(f.comboBox.getItemAt(i)).withFailMessage("check for -1").isNotEqualTo("-1");
        }

        // now add a length that is too long
        f.addTextBox.setText("10000");

        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeave(f.addButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle
                .getMessage("canNotAdd"), new Object[]{Bundle.getMessage("Length")}), Bundle.getMessage("ButtonOK"));

        assertThat(f.comboBox.getItemAt(0)).withFailMessage("1st number before bogus add").isEqualTo("32");

        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEngineAttributeEditFrameType() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.TYPE);
        // confirm that the right number of default lengths were loaded
        assertThat(f.comboBox.getItemCount()).isEqualTo(10);
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("1st type").isEqualTo("Electric");
        // now add a new type
        f.addTextBox.setText("ABC-TEST_TEST_TEST");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new type should appear at start of list
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("new type name").isEqualTo("ABC-TEST_TEST_TEST");

        // test replace
        f.comboBox.setSelectedItem("ABC-TEST_TEST_TEST");
        f.addTextBox.setText("ABCDEF-TEST");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("replaced ABC-TEST").isEqualTo("ABCDEF-TEST");

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("1st type after delete").isEqualTo("Diesel");

        // enter a type name that is too long
        f.addTextBox.setText("ABCDEFGHIJKLM-TEST");
        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeave(f.addButton);

        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotAdd"), new Object[]{Bundle.getMessage("Type")}),
                Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEngineAttributeEditFrameRoad() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.ROAD);
        // confirm that the right number of default lengths were loaded
        assertThat(f.comboBox.getItemCount()).isEqualTo(133);
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("1st road").isEqualTo("AA");
        // now add a new road
        f.addTextBox.setText("ABC-TEST");
        JemmyUtil.enterClickAndLeave(f.addButton);
        assertThat(f.comboBox.getItemAt(1)).withFailMessage("new road name").isEqualTo("ABC-TEST");
        Assert.assertEquals("Select combobox is correct", "ABC-TEST", f.comboBox.getSelectedItem());

        // test replace
        f.addTextBox.setText("ABCDEF-TEST");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        assertThat(f.comboBox.getItemAt(1)).withFailMessage("replaced ABC-TEST").isEqualTo("ABCDEF-TEST");

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        assertThat(f.comboBox.getItemAt(0)).withFailMessage("1st road after delete").isEqualTo("AA");

        // enter a road name that is too long
        f.addTextBox.setText("ABCDEFGHIJKLM-TEST");
        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeave(f.replaceButton);

        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotReplace"), new Object[]{Bundle.getMessage("Road")}),
                Bundle.getMessage("ButtonOK"));

        // enter a road name that has a reserved character
        f.addTextBox.setText("A.B");
        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeave(f.replaceButton);

        JemmyUtil.pressDialogButton(
                MessageFormat.format(Bundle.getMessage("canNotReplace"), new Object[]{Bundle.getMessage("Road")}),
                Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEngineAttributeEditFrame2() {
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.OWNER);
        JUnitUtil.dispose(f);
        f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.CONSIST);
        JUnitUtil.dispose(f);

        JUnitUtil.dispose(f);
    }
}
