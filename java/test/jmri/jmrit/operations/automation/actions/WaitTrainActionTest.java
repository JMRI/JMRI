package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WaitTrainActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        WaitTrainAction t = new WaitTrainAction();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testActionNoAutomationItem() {
        WaitTrainAction action = new WaitTrainAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        WaitTrainAction action = new WaitTrainAction();
        Assert.assertEquals("name", Bundle.getMessage("WaitForTrain"), action.getName());
    }
    
    @Test
    public void testIsMessageOkEnabled() {
        WaitTrainAction action = new WaitTrainAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }
    
    @Test
    public void testIsMessageFailEnabled() {
        WaitTrainAction action = new WaitTrainAction();
        Assert.assertTrue(action.isMessageFailEnabled());
    }
    
    @Test
    public void testIsConcurrentAction() {
        WaitTrainAction action = new WaitTrainAction();
        Assert.assertTrue(action.isConcurrentAction());
    }

    @Test
    public void testActionWaitForTrainToBuild() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        WaitTrainAction action = new WaitTrainAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        Assert.assertEquals("action item status", "", automationItem.getStatus());

        // does nothing, no train assignment
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
        Assert.assertEquals("action item status", Bundle.getMessage("FAILED"), automationItem.getStatus());

        automationItem.setTrain(train1);
        action.doAction();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
        Assert.assertEquals("action item status", Bundle.getMessage("Running"), automationItem.getStatus());

        //try again, waiting for train to build
        action.doAction();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
        Assert.assertEquals("action item status", Bundle.getMessage("Running"), automationItem.getStatus());

        train1.build();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertTrue(automationItem.isActionSuccessful());
        Assert.assertEquals("action item status", Bundle.getMessage("ButtonOK"), automationItem.getStatus());
    }

    @Test
    public void testActionWaitForTrainBuildEnableReset() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // confirm default
        Assert.assertTrue(train1.isBuildEnabled());

        WaitTrainAction action = new WaitTrainAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        // does nothing, no train assignment
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        automationItem.setTrain(train1);
        action.doAction();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        //try again, waiting for train to build
        action.doAction();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        train1.setBuildEnabled(false);
        
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertTrue(automationItem.isActionSuccessful());
    }

    @Test
    public void testActionWaitForTrainToReachLocation() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // confirm default
        Assert.assertTrue(train1.isBuildEnabled());

        WaitTrainAction action = new WaitTrainAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        // does nothing, no train assignment
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        automationItem.setTrain(train1);
        // wait for train to reach last location in route
        automationItem.setRouteLocation(train1.getTrainTerminatesRouteLocation());
        action.doAction();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
        
        String message = action.getActionString();
        Assert.assertTrue("message contains route name", message.contains(train1.getTrainTerminatesRouteLocation().getName()));

        // There are 3 locations in the route
        Assert.assertTrue(train1.build());
        train1.move();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        // next move is the last location
        train1.move();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertTrue(automationItem.isActionSuccessful());
    }
    
    @Test
    public void testCancel() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // confirm default
        Assert.assertTrue(train1.isBuildEnabled());

        WaitTrainAction action = new WaitTrainAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        automationItem.setTrain(train1);
        action.doAction();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        // cancel
        action.cancelAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
    }

    // private final static Logger log = LoggerFactory.getLogger(WaitTrainActionTest.class);

}
