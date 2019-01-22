package jmri.jmrit.operations.automation;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.actions.ActionCodes;
import jmri.jmrit.operations.automation.actions.ActivateTrainScheduleAction;
import jmri.jmrit.operations.automation.actions.BuildTrainAction;
import jmri.jmrit.operations.automation.actions.GotoAction;
import jmri.jmrit.operations.automation.actions.RunAutomationAction;
import jmri.jmrit.operations.automation.actions.WaitTrainAction;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import org.junit.Assert;
import org.junit.Test;

public class AutomationItemTest extends OperationsTestCase {

    @Test
    public void testDefaults() {
        AutomationItem automationItem = new AutomationItem("TestId");
        Assert.assertNotNull("test creation", automationItem);
        Assert.assertEquals("test id", "TestId", automationItem.getId());
        Assert.assertEquals("test id", "TestId", automationItem.toString());
        Assert.assertEquals(ActionCodes.NO_ACTION, automationItem.getActionCode());
        Assert.assertEquals("Do Nothing", automationItem.getAction().getName());      
        Assert.assertEquals("Do Nothing", automationItem.getActionName());
        Assert.assertEquals("", automationItem.getMessage());
        Assert.assertEquals("", automationItem.getMessageFail());
        Assert.assertEquals(0, automationItem.getSequenceId());
        Assert.assertEquals("", automationItem.getStatus());
        Assert.assertEquals("", automationItem.getTrainScheduleId());
        
        Assert.assertEquals(null, automationItem.getAutomationToRun());
        Assert.assertEquals(null, automationItem.getGotoAutomationItem());
        Assert.assertEquals(null, automationItem.getRouteLocation());
        Assert.assertEquals(null, automationItem.getTrain());
        Assert.assertEquals(null, automationItem.getTrainSchedule());
        
        // static tests
        Assert.assertEquals("Do Nothing", AutomationItem.getActionByCode(0x0000).getName()); // there isn't a code 0x0000 action
        Assert.assertEquals("Number of actions", 29, AutomationItem.getActionComboBox().getItemCount());
        Assert.assertEquals("Number of actions", 29, AutomationItem.getActionList().size());
    }

    @Test
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

    @Test
    public void testAutomationToRun() {
        AutomationItem automationItem = new AutomationItem("TestId");
        Assert.assertNotNull("test creation", automationItem);
        Assert.assertEquals("test id", "TestId", automationItem.getId());

        Automation automation = new Automation("101", "testAutomationName");
        automationItem.setAutomationToRun(automation);
        Assert.assertEquals("Do nothing action can't have an automation assignment", null, automationItem.getAutomationToRun());

        automationItem.setAction(new RunAutomationAction());
        Assert.assertEquals("Automation hasn't been registered with manager", null, automationItem.getAutomationToRun());

        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        manager.register(automation);

        Assert.assertEquals("Run automation action now registered", automation, automationItem.getAutomationToRun());

        automationItem.setAutomationToRun(null);
        Assert.assertEquals(null, automationItem.getAutomationToRun());

        automationItem.setOther(automation);
        Assert.assertEquals(automation, automationItem.getAutomationToRun());
    }

    @Test
    public void testGotoAutomationItem() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
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

    @Test
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

    @Test
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

    @Test
    public void testTrainSchedule() {
        AutomationItem automationItem = new AutomationItem("TestId");
        Assert.assertNotNull("test creation", automationItem);
        Assert.assertEquals("test id", "TestId", automationItem.getId());

        TrainSchedule trainSchedule = InstanceManager.getDefault(TrainScheduleManager.class).newSchedule("TestScheduleName");
        automationItem.setTrainSchedule(trainSchedule);

        Assert.assertEquals("Do nothing action can't have a train schedule assignment", null, automationItem.getTrainSchedule());
        automationItem.setAction(new ActivateTrainScheduleAction());
        Assert.assertEquals(trainSchedule, automationItem.getTrainSchedule());

        automationItem.setTrainSchedule(null);
        Assert.assertEquals(null, automationItem.getTrainSchedule());

        automationItem.setOther(trainSchedule);
        Assert.assertEquals(trainSchedule, automationItem.getTrainSchedule());
    }
}
