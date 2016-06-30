//AutomationItemTest.java
package jmri.jmrit.operations.automation;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.actions.ActivateTimetableAction;
import jmri.jmrit.operations.automation.actions.BuildTrainAction;
import jmri.jmrit.operations.automation.actions.GotoAction;
import jmri.jmrit.operations.automation.actions.MoveTrainAction;
import jmri.jmrit.operations.automation.actions.RunAutomationAction;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.timetable.TrainSchedule;
import jmri.jmrit.operations.trains.timetable.TrainScheduleManager;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AutomationManagerTest extends OperationsTestCase {

    public void testDefaults() {
        AutomationManager manager = AutomationManager.instance();
        Assert.assertNotNull("test creation", manager);
        Assert.assertEquals(0, manager.getSize());
        Assert.assertEquals(null, manager.getAutomationById(""));
        Assert.assertEquals(null, manager.getAutomationByName(""));
        Assert.assertEquals(0, manager.getAutomationsByIdList().size());
        Assert.assertEquals(0, manager.getAutomationsByNameList().size());
        Assert.assertEquals("Only null selection available", 1, manager.getComboBox().getItemCount());
    }
    
    public void testCreateAutomation() {
        AutomationManager manager = AutomationManager.instance();
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
     * Creates an automation with 5 items, and checks to see if all items
     * are copied correctly.
     */
    public void testCopyAutomation() {
        AutomationManager manager = AutomationManager.instance();
        Assert.assertNotNull("test creation", manager);
        Automation automation = manager.newAutomation("TestAutomation");
        automation.setComment("test comment for automation");
        Assert.assertEquals(1, manager.getSize());
        
        AutomationItem item1 = automation.addItem();
        item1.setAction(new BuildTrainAction());
        item1.setTrain(new Train("trainId","trainName1"));
        item1.setMessage("item1 OK message");
        item1.setMessageFail("item1 fail message");
        item1.setHaltFailureEnabled(false);
               
        AutomationItem item2 = automation.addItem();
        item2.setAction(new GotoAction());
        item2.setGotoAutomationItem(item1);
        
        AutomationItem item3 = automation.addItem();
        item3.setAction(new MoveTrainAction());
        item3.setTrain(new Train("trainId","trainName2"));
        item3.setRouteLocation(new RouteLocation("id", new Location("id", "testLocationName")));
        
        AutomationItem item4 = automation.addItem();
        item4.setAction(new ActivateTimetableAction());
        TrainSchedule trainSchedule = TrainScheduleManager.instance().newSchedule("train schedule name");
        item4.setOther(trainSchedule);
        
        AutomationItem item5 = automation.addItem();
        item5.setAction(new RunAutomationAction());
        Automation automationToRun = manager.newAutomation("TestAutomation2");
        item5.setOther(automationToRun);
        item5.setMessage("item5 OK message");
        item5.setMessageFail("item5 fail message");
        item5.setHaltFailureEnabled(false);
               
        Automation copy = manager.copyAutomation(automation, "Copy");
        Assert.assertNotNull("test automation creation", copy);
        
        // There are now three automations
        Assert.assertEquals("The number of automations", 3, manager.getSize());
        Assert.assertEquals("The number of items", 5, copy.getSize());
        
        Assert.assertEquals(copy.getComment(), automation.getComment());
        
        AutomationItem copyItem1 = copy.getItemBySequenceId(1);
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
        
        AutomationItem copyItem2 = copy.getItemBySequenceId(2);
        Assert.assertEquals("2nd item is goto", copyItem2.getActionName(), item2.getActionName());
        Assert.assertNull(copyItem2.getTrain());
        Assert.assertNotNull(copyItem2.getGotoAutomationItem());
        Assert.assertNull(copyItem2.getTrainSchedule());
        Assert.assertNull(copyItem2.getRouteLocation());
        Assert.assertEquals(copyItem2.getGotoAutomationItem().getActionName(), item2.getGotoAutomationItem().getActionName());
        Assert.assertNull(copyItem2.getAutomationToRun());
        Assert.assertEquals("", copyItem2.getMessage());
        Assert.assertEquals("", copyItem2.getMessageFail());
        Assert.assertTrue(copyItem2.isHaltFailureEnabled());
       
        AutomationItem copyItem3 = copy.getItemBySequenceId(3);
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
        
        AutomationItem copyItem4 = copy.getItemBySequenceId(4);
        Assert.assertEquals("4th item is activate train schedule", copyItem4.getActionName(), item4.getActionName());
        Assert.assertNull(copyItem4.getTrain());
        Assert.assertNull(copyItem4.getGotoAutomationItem());
        Assert.assertNull(copyItem4.getRouteLocation());
        Assert.assertNotNull(copyItem4.getTrainSchedule());
        Assert.assertEquals(trainSchedule, copyItem4.getTrainSchedule());
        Assert.assertNull(copyItem4.getAutomationToRun());
        Assert.assertEquals("", copyItem4.getMessage());
        Assert.assertEquals("", copyItem4.getMessageFail());
        Assert.assertTrue(copyItem4.isHaltFailureEnabled());

        AutomationItem copyItem5 = copy.getItemBySequenceId(5);
        Assert.assertEquals("5th item is run automation", copyItem5.getActionName(), item5.getActionName());
        Assert.assertNull(copyItem5.getTrain());
        Assert.assertNull(copyItem5.getGotoAutomationItem());
        Assert.assertNull(copyItem5.getRouteLocation());
        Assert.assertNull(copyItem5.getTrainSchedule());
        Assert.assertNotNull(copyItem5.getAutomationToRun());
        Assert.assertEquals(automationToRun, copyItem5.getAutomationToRun());
        Assert.assertEquals("item5 OK message", copyItem5.getMessage());
        Assert.assertEquals("item5 fail message", copyItem5.getMessageFail());
        Assert.assertFalse(copyItem5.isHaltFailureEnabled());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public AutomationManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AutomationManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AutomationManagerTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
