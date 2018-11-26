package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Engines GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2010
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
        // new model was next
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
        // new model was next
        Assert.assertEquals("1st number after delete", "32", f.comboBox.getItemAt(0));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testEngineAttributeEditFrameLengthCm() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.LENGTH);
        // confirm that the right number of default lengths were loaded
        Assert.assertEquals(29, f.comboBox.getItemCount());
        // now add a new length in inches
        f.addTextBox.setText("10" + "cm");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new length should appear at start of list
        Assert.assertEquals("new length name", "8", f.comboBox.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.deleteButton);
        // new model was next
        Assert.assertEquals("1st number after delete", "32", f.comboBox.getItemAt(0));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEngineAttributeEditFrame2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EngineAttributeEditFrame f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.LENGTH);
        JUnitUtil.dispose(f);
        f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.OWNER);
        JUnitUtil.dispose(f);
        f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.ROAD);
        JUnitUtil.dispose(f);
        f = new EngineAttributeEditFrame();
        f.initComponents(EngineAttributeEditFrame.TYPE);
        JUnitUtil.dispose(f);
    }

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
