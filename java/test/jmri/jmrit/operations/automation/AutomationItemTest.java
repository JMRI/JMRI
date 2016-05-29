//AutomationItemTest.java
package jmri.jmrit.operations.automation;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.actions.ActionCodes;
import jmri.jmrit.operations.automation.actions.ActivateTimetableAction;
import jmri.jmrit.operations.automation.actions.BuildTrainAction;
import jmri.jmrit.operations.automation.actions.GotoAction;
import jmri.jmrit.operations.automation.actions.RunAutomationAction;
import jmri.jmrit.operations.automation.actions.WaitTrainAction;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.timetable.TrainSchedule;
import jmri.jmrit.operations.trains.timetable.TrainScheduleManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AutomationItemTest extends OperationsTestCase {

    public void testDefaults() {
        AutomationItem automationItem = new AutomationItem("TestId");
        Assert.assertNotNull("test creation", automationItem);
        Assert.assertEquals("test id", "TestId", automationItem.getId());
        Assert.assertEquals("test id", "TestId", automationItem.toString());
        Assert.assertEquals(ActionCodes.NO_ACTION, automationItem.getActionCode());     
        Assert.assertEquals("Do Nothing", automationItem.getAction().getName());
        Assert.assertEquals("Do Nothing", automationItem.getActionByCode(0x0000).getName()); // there isn't a code 0x0000 action
        Assert.assertEquals("Do Nothing", automationItem.getActionName());
        Assert.assertEquals("", automationItem.getMessage());
        Assert.assertEquals("", automationItem.getMessageFail());
        Assert.assertEquals(0, automationItem.getSequenceId());
        Assert.assertEquals("", automationItem.getStatus());
        Assert.assertEquals("", automationItem.getTrainScheduleId());
        Assert.assertEquals("Number of actions", 29, automationItem.getActionComboBox().getItemCount());
        Assert.assertEquals("Number of actions", 29, automationItem.getActionList().size());
        Assert.assertEquals(null, automationItem.getAutomationToRun());
        Assert.assertEquals(null, automationItem.getGotoAutomationItem());
        Assert.assertEquals(null, automationItem.getRouteLocation());
        Assert.assertEquals(null, automationItem.getTrain());
        Assert.assertEquals(null, automationItem.getTrainSchedule());
    }
    
    public void testMessages() {
        AutomationItem automationItem = new AutomationItem("TestId");
        Assert.assertNotNull("test creation", automationItem);
        Assert.assertEquals("test id", "TestId", automationItem.getId());
        
        automationItem.setMessage("Test OK message");
        Assert.assertEquals("Test OK message", automationItem.getMessage());
        Assert.assertEquals("", automationItem.getMessageFail());
        
        automationItem.setMessageFail("Test Fail message");
        Assert.assertEquals("Test OK message", automationItem.getMessage());
        Assert.assertEquals("Test Fail message", automationItem.getMessageFail());
    }
    
    public void testAutomationToRun() {
        AutomationItem automationItem = new AutomationItem("TestId");
        Assert.assertNotNull("test creation", automationItem);
        Assert.assertEquals("test id", "TestId", automationItem.getId());
        
        Automation automation = new Automation("101", "testAutomationName");
        automationItem.setAutomationToRun(automation);
        Assert.assertEquals("Do nothing action can't have an automation assignment", null, automationItem.getAutomationToRun());
        
        automationItem.setAction(new RunAutomationAction());
        Assert.assertEquals("Automation hasn't been registered with manager", null, automationItem.getAutomationToRun());
        
        AutomationManager manager = AutomationManager.instance();
        manager.register(automation);
      
        Assert.assertEquals("Run automation action now registered", automation, automationItem.getAutomationToRun());
        
        automationItem.setAutomationToRun(null);
        Assert.assertEquals(null, automationItem.getAutomationToRun());
        
        automationItem.setOther(automation);
        Assert.assertEquals(automation, automationItem.getAutomationToRun());
    }
    
    public void testGotoAutomationItem() {
        AutomationManager manager = AutomationManager.instance();
        Automation automation = manager.newAutomation("testAutomationGoto");
        AutomationItem item1 = automation.addItem();
        AutomationItem item2 = automation.addItem();
        
        item1.setGotoAutomationItem(item2);       
        Assert.assertEquals("Do nothing action can't have a goto item", null, item1.getGotoAutomationItem());
        
        item1.setAction(new GotoAction());      
        Assert.assertEquals(item2, item1.getGotoAutomationItem());
        
        item1.setGotoAutomationItem(null);       
        Assert.assertEquals(null, item1.getGotoAutomationItem());
        
        item1.setOther(item2);
        Assert.assertEquals(item2, item1.getGotoAutomationItem());
    }
    
    public void testRouteLocation() {
        AutomationItem automationItem = new AutomationItem("TestId");
        Assert.assertNotNull("test creation", automationItem);
        Assert.assertEquals("test id", "TestId", automationItem.getId());
        
        RouteLocation rl = new RouteLocation("testId", new Location("testId", "testLocationName"));
        automationItem.setRouteLocation(rl);
        Assert.assertEquals("Do nothing action can't have a routeLocation", null, automationItem.getRouteLocation());
        
        automationItem.setAction(new WaitTrainAction());
        Assert.assertEquals(rl, automationItem.getRouteLocation());
    }
    
    public void testTrain() {
        AutomationItem automationItem = new AutomationItem("TestId");
        Assert.assertNotNull("test creation", automationItem);
        Assert.assertEquals("test id", "TestId", automationItem.getId());
        
        Train train = new Train("TestTrainId", "TestTrainName");
        automationItem.setTrain(train);
        Assert.assertEquals("Do nothing action can't have a train assignment", null, automationItem.getTrain());
        
        automationItem.setAction(new BuildTrainAction());
        Assert.assertEquals("Build train action", train, automationItem.getTrain());       
    }
    
    public void testTrainSchedule() {
        AutomationItem automationItem = new AutomationItem("TestId");
        Assert.assertNotNull("test creation", automationItem);
        Assert.assertEquals("test id", "TestId", automationItem.getId());
        
        TrainSchedule trainSchedule = TrainScheduleManager.instance().newSchedule("TestScheduleName");
        automationItem.setTrainSchedule(trainSchedule);
        
        Assert.assertEquals("Do nothing action can't have a train schedule assignment", null, automationItem.getTrainSchedule());
        automationItem.setAction(new ActivateTimetableAction());
        Assert.assertEquals(trainSchedule, automationItem.getTrainSchedule());
        
        automationItem.setTrainSchedule(null);
        Assert.assertEquals(null, automationItem.getTrainSchedule());
        
        automationItem.setOther(trainSchedule);
        Assert.assertEquals(trainSchedule, automationItem.getTrainSchedule());
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public AutomationItemTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AutomationItemTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AutomationItemTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
