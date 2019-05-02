package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.setup.Control;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for the Operations CarLoadEditFrame class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class CarLoadEditFrameTest extends OperationsTestCase {

    @Test
    public void testCarLoadEditFrameAddButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarLoadEditFrame f = new CarLoadEditFrame();
        f.initComponents("Boxcar", "");
        f.addTextBox.setText("New Load");
        JemmyUtil.enterClickAndLeave(f.addButton);
        // new load should appear at start of list
        Assert.assertEquals("new load", "New Load", f.loadComboBox.getItemAt(0));

        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        Assert.assertTrue("exists", cl.containsName("Boxcar", "New Load"));

        JUnitUtil.dispose(f);
    }
    
    /**
     * Test load name contains the split char.  Should be rejected.
     */
    @Test
    public void testCarLoadEditFrameAddButtonException1() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarLoadEditFrame f = new CarLoadEditFrame();
        f.initComponents("Boxcar", "");
        f.addTextBox.setText("A" + CarLoad.SPLIT_CHAR + "B");
        JemmyUtil.enterClickAndLeave(f.addButton);
        
        // error dialog window should appear
        JemmyUtil.pressDialogButton(Bundle.getMessage("canNotUseLoadName"), Bundle.getMessage("ButtonOK"));

        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        Assert.assertFalse("exists", cl.containsName("Boxcar", "A" + CarLoad.SPLIT_CHAR + "B"));

        JUnitUtil.dispose(f);
    }
    
    /**
     * test load name too long.
     */
    @Test
    public void testCarLoadEditFrameAddButtonException2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarLoadEditFrame f = new CarLoadEditFrame();
        f.initComponents("Boxcar", "");
        
        // create too long load name
        StringBuffer sb = new StringBuffer("A");
        for (int i = 0; i < Control.max_len_string_attibute; i++) {
            sb.append("A");
        }
        
        f.addTextBox.setText(sb.toString());
        JemmyUtil.enterClickAndLeave(f.addButton);
        
        // error dialog window should appear
        JemmyUtil.pressDialogButton(Bundle.getMessage("canNotUseLoadName"), Bundle.getMessage("ButtonOK"));

        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        Assert.assertFalse("exists", cl.containsName(sb.toString()));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarLoadEditFrameReplaceButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        // create load to replace
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        cl.addName("Boxcar", "Test Load");
        
        CarLoadEditFrame f = new CarLoadEditFrame();
        f.initComponents("Boxcar", "Test Load");
        f.addTextBox.setText("Replace Load");
        
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        
        // dialog window should appear
        JemmyUtil.pressDialogButton(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        
        Assert.assertFalse("exists", cl.containsName("Boxcar", "Test Load"));
        Assert.assertTrue("exists", cl.containsName("Boxcar", "Replace Load"));

        JUnitUtil.dispose(f);
    }
    
    /**
     * Replace default "E" name
     */
    @Test
    public void testCarLoadEditFrameReplaceButtonDefaultEmpty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        Assert.assertEquals("default empty name", "E", cl.getDefaultEmptyName());
        
        CarLoadEditFrame f = new CarLoadEditFrame();
        f.initComponents("Boxcar", "E");
        f.addTextBox.setText("Replace E");
        
        JemmyUtil.enterClickAndLeave(f.replaceButton);
        
        // dialog window should appear
        JemmyUtil.pressDialogButton(Bundle.getMessage("replaceAll"), Bundle.getMessage("ButtonYes"));
        
        Assert.assertFalse("exists", cl.containsName("Boxcar", "E"));
        Assert.assertTrue("exists", cl.containsName("Boxcar", "Replace E"));
        
        Assert.assertEquals("default empty name", "Replace E", cl.getDefaultEmptyName());

        JUnitUtil.dispose(f);
    }

    
    @Test
    public void testCarLoadEditFrameDeleteButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        // create load to delete
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        cl.addName("Boxcar", "Test Load");
        
        CarLoadEditFrame f = new CarLoadEditFrame();
        f.initComponents("Boxcar", "Test Load");
        JemmyUtil.enterClickAndLeave(f.deleteButton);
        
        Assert.assertFalse("exists", cl.containsName("Boxcar", "Test Load"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarLoadEditFrameSaveButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        // create load modify
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        cl.addName("Boxcar", "Test Load");
        
        CarLoadEditFrame f = new CarLoadEditFrame();
        f.initComponents("Boxcar", "Test Load");
        f.toggleShowQuanity();
        
        // change load type
        f.loadTypeComboBox.setSelectedItem("Empty");
        f.priorityComboBox.setSelectedItem("High");
        // add messages
        f.pickupCommentTextField.setText("test pickup message");
        f.dropCommentTextField.setText("test drop message");
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        Assert.assertEquals("Empty", cl.getLoadType("Boxcar", "Test Load"));
        Assert.assertEquals("High", cl.getPriority("Boxcar", "Test Load"));
        Assert.assertEquals("test pickup message", cl.getPickupComment("Boxcar", "Test Load"));
        Assert.assertEquals("test drop message", cl.getDropComment("Boxcar", "Test Load"));

        JUnitUtil.dispose(f);
    }
    
    /**
     * Test deleting the default loads "E" and "L".
     */
    @Test
    public void testCarLoadEditFrameDeleteButtonException() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CarLoadEditFrame f = new CarLoadEditFrame();
        f.initComponents("Boxcar", "L");
        JemmyUtil.enterClickAndLeave(f.deleteButton);
        
        // error dialog window should appear
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle
                .getMessage("canNotDelete"), new Object[]{Bundle.getMessage("Load")}), Bundle.getMessage("ButtonOK"));
        
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        Assert.assertTrue("exists", cl.containsName("Boxcar", "L"));

        JUnitUtil.dispose(f);
    }
}
