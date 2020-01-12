package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for the Operations Engines GUI class
 *
 * @author Dan Boudreau Copyright (C) 2010
 *
 */
public class EngineAttributeEditFrameTest extends OperationsTestCase {

    @Test
    public void testEngineAttributeEditFrameModel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.MODEL);
        // confirm that the right number of models were loaded
        Assert.assertEquals(27, f.comboBox.getItemCount());
        // now add a new model name
        f.addTextBox.setText("New Model");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new model should appear at start of list
        Assert.assertEquals("new model name", "New Model", f.comboBox.getItemAt(0));

        // test replace
        f.comboBox.setSelectedItem("SD45");
        f.addTextBox.setText("DS54");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        Assert.assertEquals("replaced SD45 with DS54", "DS54", f.comboBox.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        // new model was next
        Assert.assertEquals("new model after delete", "New Model", f.comboBox.getItemAt(0));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEngineAttributeEditFrameLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(29, f.comboBox.getItemCount());
        // now add a new length
        f.addTextBox.setText("12");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new length should appear at start of list
        Assert.assertEquals("new length name", "12", f.comboBox.getItemAt(0));

        // test replace
        f.comboBox.setSelectedItem("12");
        f.addTextBox.setText("13");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        Assert.assertEquals("replaced 12 with 13", "13", f.comboBox.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("1st number after delete", "32", f.comboBox.getItemAt(0));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEngineAttributeEditFrameLengthInches() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(29, f.comboBox.getItemCount());
        // now add a new length in inches
        f.addTextBox.setText("10" + "\"");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new length should appear at start of list
        Assert.assertEquals("new length name", "72", f.comboBox.getItemAt(0));

        // test replace
        f.comboBox.setSelectedItem("72");
        f.addTextBox.setText("73");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        Assert.assertEquals("replaced 72 with 73", "73", f.comboBox.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("1st number after delete", "32", f.comboBox.getItemAt(0));

        // now try error condition
        f.addTextBox.setText("A" + "\"");
        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeave(f.addButton);

        JemmyUtil.pressDialogButton(Bundle.getMessage("ErrorEngineLength"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEngineAttributeEditFrameLengthCm() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(29, f.comboBox.getItemCount());
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
        JemmyUtil.enterClickAndLeave(f.addButton);

        JemmyUtil.pressDialogButton(Bundle.getMessage("ErrorEngineLength"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEngineAttributeEditFrameLengthErrors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(29, f.comboBox.getItemCount());
        // now add a bogus length
        f.addTextBox.setText("A");
        JemmyUtil.enterClickAndLeave(f.addButton);

        jmri.util.JUnitAppender.assertErrorMessage("length not an integer");
        Assert.assertEquals("1st number before bogus add", "32", f.comboBox.getItemAt(0));

        // check for the value "A" 
        for (int i = 0; i < f.comboBox.getItemCount(); i++) {
            Assert.assertNotEquals("check for A", "A", f.comboBox.getItemAt(i));
        }

        // now add a negative length
        f.addTextBox.setText("-1");
        JemmyUtil.enterClickAndLeave(f.addButton);

        jmri.util.JUnitAppender.assertErrorMessage("engine length has to be a positive number");
        Assert.assertEquals("1st number before bogus add", "32", f.comboBox.getItemAt(0));

        // check for the value "-1" 
        for (int i = 0; i < f.comboBox.getItemCount(); i++) {
            Assert.assertNotEquals("check for -1", "-1", f.comboBox.getItemAt(i));
        }

        // now add a length that is too long
        f.addTextBox.setText("10000");

        // should cause error dialog to appear
        JemmyUtil.enterClickAndLeave(f.addButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle
                .getMessage("canNotAdd"), new Object[]{Bundle.getMessage("Length")}), Bundle.getMessage("ButtonOK"));

        Assert.assertEquals("1st number before bogus add", "32", f.comboBox.getItemAt(0));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEngineAttributeEditFrameType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.TYPE);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(10, f.comboBox.getItemCount());
        Assert.assertEquals("1st type", "Electric", f.comboBox.getItemAt(0));
        // now add a new type
        f.addTextBox.setText("ABC-TEST_TEST_TEST");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new type should appear at start of list
        Assert.assertEquals("new type name", "ABC-TEST_TEST_TEST", f.comboBox.getItemAt(0));

        // test replace
        f.comboBox.setSelectedItem("ABC-TEST_TEST_TEST");
        f.addTextBox.setText("ABCDEF-TEST");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        Assert.assertEquals("replaced ABC-TEST", "ABCDEF-TEST", f.comboBox.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("1st type after delete", "Electric", f.comboBox.getItemAt(0));

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
    public void testEngineAttributeEditFrameRoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.ROAD);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(133, f.comboBox.getItemCount());
        Assert.assertEquals("1st road", "AA", f.comboBox.getItemAt(0));
        // now add a new road
        f.addTextBox.setText("ABC-TEST");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new road should appear at start of list
        Assert.assertEquals("new road name", "ABC-TEST", f.comboBox.getItemAt(0));

        // test replace
        f.comboBox.setSelectedItem("ABC-TEST");
        f.addTextBox.setText("ABCDEF-TEST");
        // push replace button
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        // need to also push the "Yes" button in the dialog window
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        // did the replace work?
        Assert.assertEquals("replaced ABC-TEST", "ABCDEF-TEST", f.comboBox.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("1st road after delete", "AA", f.comboBox.getItemAt(0));

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
    public void testEngineAttributeEditFrame2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
