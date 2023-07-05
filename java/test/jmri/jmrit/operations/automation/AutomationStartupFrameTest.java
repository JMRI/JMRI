package jmri.jmrit.operations.automation;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutomationStartupFrameTest extends OperationsTestCase {
    
    @Test
    public void testFrameCreation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AutomationStartupFrame asf = new AutomationStartupFrame();
        Assert.assertNotNull("exists", asf);
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("MenuStartupAutomation"));
        Assert.assertNotNull("exists", f);
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testSaveButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        // create 2 automations
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        manager.newAutomation("TestAutomation 1");
        manager.newAutomation("TestAutomation 2");
        
        AutomationStartupFrame asf = new AutomationStartupFrame();
        Assert.assertNotNull("exists", asf);
        
        JFrameOperator jfo = new JFrameOperator(asf.getTitle()); 
        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        Assert.assertEquals(3, comboBox.getItemCount());
        
        comboBox.setSelectedIndex(1);       
        JemmyUtil.enterClickAndLeave(asf.saveButton);
        Assert.assertEquals("confirm startup automation", "TestAutomation 1", manager.getStartupAutomation().getName());
        
        // this should remove the startup automation
        comboBox.setSelectedIndex(0);       
        JemmyUtil.enterClickAndLeave(asf.saveButton);
        Assert.assertNull(manager.getStartupAutomation());
        
        JUnitUtil.dispose(asf);
    }
    
    @Test
    public void testTestButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        // create automation
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        manager.newAutomation("TestAutomation 1");
        
        AutomationStartupFrame asf = new AutomationStartupFrame();
        Assert.assertNotNull("exists", asf);
        
        JFrameOperator jfo = new JFrameOperator(asf.getTitle()); 
        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        Assert.assertEquals(2, comboBox.getItemCount());
        
        comboBox.setSelectedIndex(1);       
        JemmyUtil.enterClickAndLeave(asf.saveButton);
        Assert.assertEquals("confirm startup automation", "TestAutomation 1", manager.getStartupAutomation().getName());
        
        JemmyUtil.enterClickAndLeave(asf.testButton);
        
        JUnitUtil.dispose(asf);
    }
    
    @Test
    public void testPropertyChange() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        AutomationStartupFrame asf = new AutomationStartupFrame();
        Assert.assertNotNull("exists", asf);
        
        JFrameOperator jfo = new JFrameOperator(asf.getTitle()); 
        JComboBoxOperator comboBox = new JComboBoxOperator(jfo, 0);
        Assert.assertEquals(1, comboBox.getItemCount());
        
        // create 2 automations
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Automation a1 = manager.newAutomation("TestAutomation 1");
        Automation a2 = manager.newAutomation("TestAutomation 2");
        Assert.assertEquals(3, comboBox.getItemCount());
        
        manager.setStartupAutomation(a2);
        manager.deregister(a1);
        Assert.assertEquals(2, comboBox.getItemCount());
        Assert.assertEquals(a2, comboBox.getSelectedItem());
        
        JUnitUtil.dispose(asf);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AutomationStartupFrame tf = new AutomationStartupFrame();
        tf.initComponents();

        JFrameOperator jfo = new JFrameOperator(tf.getTitle());
        Assert.assertNotNull("visible and found", jfo);

        // confirm window appears
        JmriJFrame f = JmriJFrame.getFrame(tf.getTitle());
        Assert.assertNotNull("exists", f);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        f = JmriJFrame.getFrame(tf.getTitle());
        Assert.assertNotNull("exists", f);
        // now close window with save button
        Setup.setCloseWindowOnSaveEnabled(true);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        jfo.waitClosed();
        // confirm window is closed
        f = JmriJFrame.getFrame(tf.getTitle());
        Assert.assertNull("does not exist", f);
    }
}
