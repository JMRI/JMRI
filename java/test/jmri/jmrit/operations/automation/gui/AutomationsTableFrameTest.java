package jmri.jmrit.operations.automation.gui;

import org.junit.Assert;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.*;
import jmri.jmrit.operations.setup.Control;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class AutomationsTableFrameTest extends OperationsTestCase {

    @Test
    public void testFrameCreation() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationsTableFrame f = new AutomationsTableFrame();
        Assert.assertNotNull("test creation", f);

        // confirm that the add automation frame isn't available
        JmriJFrame addAutomationFrame = JmriJFrame.getFrame(Bundle.getMessage("TitleAutomationAdd"));
        Assert.assertNull(addAutomationFrame);

        // now create the add automation frame
        f.addButton.doClick();
        // the following fails on a 13" laptop
        //JemmyUtil.enterClickAndLeave(f.addButton);
        addAutomationFrame = JmriJFrame.getFrame(Bundle.getMessage("TitleAutomationAdd"));
        Assert.assertNotNull(addAutomationFrame);

        JUnitUtil.dispose(addAutomationFrame);
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testTable() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationsTableFrame f = new AutomationsTableFrame();
        Assert.assertNotNull("test creation", f);
        
        // show entire table
        f.setSize(1200, Control.panelHeight400);
        
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        
        Automation automation = manager.newAutomation("TestAutomation");
        automation.setComment("Test Automation Comment");
        automation.addItem();
        automation.run();
        
        Assert.assertEquals("table size", 1, f.automationsModel.getRowCount());        
        Assert.assertEquals("Confirm Comment", "Test Automation Comment", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Comment"))));
        
        // it can take awhile for the the automation to complete
        JUnitUtil.waitFor(() -> {
            return "OK".equals(tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Status"))));
        }, "Wait for status");
        Assert.assertEquals("Confirm Status", "OK", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Status"))));
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testDeleteButton() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationsTableFrame f = new AutomationsTableFrame();
        Assert.assertNotNull("test creation", f);
        
        // show entire table
        f.setSize(1200, Control.panelHeight400);
        
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        
        Automation automation1 = manager.newAutomation("TestAutomation1");
        automation1.setComment("Test Automation Comment 1");
        automation1.addItem();
 
        Automation automation2 = manager.newAutomation("TestAutomation2");
        automation2.setComment("Test Automation Comment 2");
        automation2.addItem();
        
        Assert.assertEquals("table size", 2, f.automationsModel.getRowCount());
        JemmyUtil.clickOnCellThreadSafe(tbl, 0, Bundle.getMessage("ButtonDelete"));
        JemmyUtil.pressDialogButton(Bundle.getMessage("DeleteAutomation?"), Bundle.getMessage("ButtonNo"));
        Assert.assertEquals("table size", 2, f.automationsModel.getRowCount());
        
        JemmyUtil.clickOnCellThreadSafe(tbl, 0, Bundle.getMessage("ButtonDelete"));
        JemmyUtil.pressDialogButton(Bundle.getMessage("DeleteAutomation?"), Bundle.getMessage("ButtonYes"));
        Assert.assertEquals("table size", 1, f.automationsModel.getRowCount());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testEditButton() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationsTableFrame f = new AutomationsTableFrame();
        Assert.assertNotNull("test creation", f);
        
        // show entire table
        f.setSize(1200, Control.panelHeight400);
        
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        
        Automation automation1 = manager.newAutomation("TestAutomation1");
        automation1.setComment("Test Automation Comment 1");
        automation1.addItem();
        
        Assert.assertEquals("table size", 1, f.automationsModel.getRowCount());
        JemmyUtil.clickOnCellThreadSafe(tbl, 0, Bundle.getMessage("ButtonEdit"));
        
        // confirm edit frame exists
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("TitleAutomationEdit")) != null;
        },"edit frame exists");

        // dispose also closes edit frame
        JUnitUtil.dispose(f);
        JmriJFrame sef = JmriJFrame.getFrame(Bundle.getMessage("TitleAutomationEdit"));
        Assert.assertNull(sef);
    }
    
    @Test
    public void testRunButton() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationsTableFrame f = new AutomationsTableFrame();
        Assert.assertNotNull("test creation", f);
        
        // show entire table
        f.setSize(1200, Control.panelHeight400);
        
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        
        Automation automation1 = manager.newAutomation("TestAutomation1");
        automation1.setComment("Test Automation Comment 1");
        AutomationItem item = automation1.addItem();
        item.setMessage("Hello Test");
        
        Assert.assertEquals("table size", 1, f.automationsModel.getRowCount());
        JemmyUtil.clickOnCellThreadSafe(tbl, 0, Bundle.getMessage("Run"));
        JemmyUtil.pressDialogButton("1c1  Do Nothing", Bundle.getMessage("ButtonOK"));
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testSortRadioButton() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationsTableFrame f = new AutomationsTableFrame();
        Assert.assertNotNull("test creation", f);
        
        Automation automation1 = manager.newAutomation("TestAutomationB");
        automation1.setComment("Test Automation Comment 1");
        automation1.addItem();
 
        Automation automation2 = manager.newAutomation("TestAutomationA");
        automation2.setComment("Test Automation Comment 2");
        automation2.addItem();
        
        Assert.assertEquals("table size", 2, f.automationsModel.getRowCount());
        
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);

        // default is short by Name
        Assert.assertEquals("Confirm Name", "TestAutomationA", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Name"))));
        
        JemmyUtil.enterClickAndLeave(f.sortByIdRadioButton);
        Assert.assertEquals("Confirm Name", "TestAutomationB", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Name"))));
        JUnitUtil.dispose(f);
    }
}
