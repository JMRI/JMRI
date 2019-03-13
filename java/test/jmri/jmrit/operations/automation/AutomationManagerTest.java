package jmri.jmrit.operations.automation;

import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.JUnitOperationsUtil;
import org.junit.Assert;
import org.junit.Test;

public class AutomationManagerTest extends OperationsTestCase {

    @Test
    public void testDefaults() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertNotNull("test creation", manager);
        Assert.assertEquals(0, manager.getSize());
        Assert.assertEquals(null, manager.getAutomationById(""));
        Assert.assertEquals(null, manager.getAutomationByName(""));
        Assert.assertEquals(0, manager.getAutomationsByIdList().size());
        Assert.assertEquals(0, manager.getAutomationsByNameList().size());
        Assert.assertEquals("Only null selection available", 1, manager.getComboBox().getItemCount());
    }

    @Test
    public void testCreateAutomation() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertNotNull("test creation", manager);
        Automation automation = manager.newAutomation("TestAutomation");
        Assert.assertNotNull("test automation creation", automation);
        Assert.assertEquals("TestAutomation", automation.getName());
        Assert.assertEquals(1, manager.getSize());

        Automation automation2 = manager.newAutomation("TestAutomation2");
        Assert.assertEquals(2, manager.getSize());

        Assert.assertEquals(automation, manager.getAutomationByName("TestAutomation"));
        Assert.assertEquals(automation2, manager.getAutomationByName("TestAutomation2"));

        String id = automation.getId();
        Assert.assertEquals(automation, manager.getAutomationById(id));

        // confirm that previous automation is returned
        Automation automation3 = manager.newAutomation("TestAutomation");
        Assert.assertEquals(automation3, manager.getAutomationByName("TestAutomation"));
        Assert.assertEquals(2, manager.getSize());
    }

    /**
     * Creates an automation with 5 items, and checks to see if all items are
     * copied correctly.
     */
    @Test
    public void testCopyAutomation() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertNotNull("test creation", manager);

        Automation automation = JUnitOperationsUtil.createAutomation();
        Assert.assertNotNull("test creation", automation);

        Automation automationCopy = manager.copyAutomation(automation, "Copy");
        Assert.assertNotNull("test automation creation", automationCopy);

        // There are now three automations
        Assert.assertEquals("The number of automations", 3, manager.getSize());
        Assert.assertEquals("The number of items", 5, automationCopy.getSize());

        Assert.assertEquals(automationCopy.getComment(), automation.getComment());

        AutomationItem item1 = automation.getItemBySequenceId(1);
        AutomationItem copyItem1 = automationCopy.getItemBySequenceId(1);
        Assert.assertEquals("1st item is build train", copyItem1.getActionName(), item1.getActionName());
        Assert.assertNotNull(copyItem1.getTrain());
        Assert.assertNull(copyItem1.getGotoAutomationItem());
        Assert.assertNull(copyItem1.getTrainSchedule());
        Assert.assertNull(copyItem1.getRouteLocation());
        Assert.assertEquals(copyItem1.getTrain(), item1.getTrain());
        Assert.assertEquals("item1 OK message", copyItem1.getMessage());
        Assert.assertEquals("item1 fail message", copyItem1.getMessageFail());
        Assert.assertNull(copyItem1.getAutomationToRun());
        Assert.assertFalse(copyItem1.isHaltFailureEnabled());

        AutomationItem item2 = automation.getItemBySequenceId(2);
        AutomationItem copyItem2 = automationCopy.getItemBySequenceId(2);
        Assert.assertEquals("2nd item is goto", copyItem2.getActionName(), item2.getActionName());
        Assert.assertNull(copyItem2.getTrain());
        Assert.assertNotNull(copyItem2.getGotoAutomationItem());
        Assert.assertNull(copyItem2.getTrainSchedule());
        Assert.assertNull(copyItem2.getRouteLocation());
        Assert.assertEquals(copyItem2.getGotoAutomationItem().getActionName(),
                item2.getGotoAutomationItem().getActionName());
        Assert.assertNull(copyItem2.getAutomationToRun());
        Assert.assertEquals("", copyItem2.getMessage());
        Assert.assertEquals("", copyItem2.getMessageFail());
        Assert.assertTrue(copyItem2.isHaltFailureEnabled());

        AutomationItem item3 = automation.getItemBySequenceId(3);
        AutomationItem copyItem3 = automationCopy.getItemBySequenceId(3);
        Assert.assertEquals("3rd item is move train", copyItem3.getActionName(), item3.getActionName());
        Assert.assertNotNull(copyItem3.getTrain());
        Assert.assertNull(copyItem3.getGotoAutomationItem());
        Assert.assertNull(copyItem3.getTrainSchedule());
        Assert.assertNotNull(copyItem3.getRouteLocation());
        Assert.assertEquals(copyItem3.getTrain(), item3.getTrain());
        Assert.assertEquals(copyItem3.getRouteLocation(), item3.getRouteLocation());
        Assert.assertNull(copyItem3.getAutomationToRun());
        Assert.assertEquals("", copyItem3.getMessage());
        Assert.assertEquals("", copyItem3.getMessageFail());
        Assert.assertTrue(copyItem3.isHaltFailureEnabled());

        AutomationItem item4 = automation.getItemBySequenceId(4);
        AutomationItem copyItem4 = automationCopy.getItemBySequenceId(4);
        Assert.assertEquals("4th item is activate train schedule", copyItem4.getActionName(), item4.getActionName());
        Assert.assertNull(copyItem4.getTrain());
        Assert.assertNull(copyItem4.getGotoAutomationItem());
        Assert.assertNull(copyItem4.getRouteLocation());
        Assert.assertNotNull(copyItem4.getTrainSchedule());
        Assert.assertEquals(copyItem4.getTrainSchedule(), item4.getTrainSchedule());
        Assert.assertNull(copyItem4.getAutomationToRun());
        Assert.assertEquals("", copyItem4.getMessage());
        Assert.assertEquals("", copyItem4.getMessageFail());
        Assert.assertTrue(copyItem4.isHaltFailureEnabled());

        AutomationItem item5 = automation.getItemBySequenceId(5);
        AutomationItem copyItem5 = automationCopy.getItemBySequenceId(5);
        Assert.assertEquals("5th item is run automation", copyItem5.getActionName(), item5.getActionName());
        Assert.assertNull(copyItem5.getTrain());
        Assert.assertNull(copyItem5.getGotoAutomationItem());
        Assert.assertNull(copyItem5.getRouteLocation());
        Assert.assertNull(copyItem5.getTrainSchedule());
        Assert.assertNotNull(copyItem5.getAutomationToRun());
        Assert.assertEquals(copyItem5.getAutomationToRun(), item5.getAutomationToRun());
        Assert.assertEquals("item5 OK message", copyItem5.getMessage());
        Assert.assertEquals("item5 fail message", copyItem5.getMessageFail());
        Assert.assertFalse(copyItem5.isHaltFailureEnabled());
    }
    
    @Test
    public void testGetAutomationsById() { 
        Automation automation = JUnitOperationsUtil.createAutomation();
        AutomationManager automationManager = InstanceManager.getDefault(AutomationManager.class);
        
        List<Automation> list = automationManager.getAutomationsByIdList();
        Assert.assertEquals("First automation created",  automation, list.get(0));
    }
    
    @Test
    public void testGetAutomationsByName() { 
        Automation automation = JUnitOperationsUtil.createAutomation();
        AutomationManager automationManager = InstanceManager.getDefault(AutomationManager.class);
        
        List<Automation> list = automationManager.getAutomationsByNameList();
        Assert.assertEquals("2nd in name list",  automation, list.get(1));
    }
    
    @Test
    public void testRemoveAutomation() { 
        Automation automation = JUnitOperationsUtil.createAutomation();
        AutomationManager automationManager = InstanceManager.getDefault(AutomationManager.class);
     // confirm two automations
        Assert.assertEquals("number of automations", 2,  automationManager.getSize());
        
        automationManager.deregister(automation);
        Assert.assertEquals("number of automations", 1,  automationManager.getSize());

    }
    
    @Test
    public void testXmlReadWrite() {     
        AutomationManager automationManager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("number of automations", 0,  automationManager.getSize());
        
        // create automations to save
        JUnitOperationsUtil.createAutomation();
        // confirm two automations
        Assert.assertEquals("number of automations", 2,  automationManager.getSize());
        
        // automation elements are stored in the trains file
        InstanceManager.getDefault(TrainManagerXml.class).writeOperationsFile();
        
        // remove all
        automationManager.dispose();
        Assert.assertEquals("number of automations", 0,  automationManager.getSize());
        
        // restore
        InstanceManager.getDefault(TrainManagerXml.class).initialize();
        
        // confirm two automations restored
        Assert.assertEquals("number of automations", 2,  automationManager.getSize());
    }
}
