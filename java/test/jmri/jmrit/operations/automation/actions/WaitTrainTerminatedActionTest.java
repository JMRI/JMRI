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
public class WaitTrainTerminatedActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        WaitTrainTerminatedAction t = new WaitTrainTerminatedAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        WaitTrainTerminatedAction action = new WaitTrainTerminatedAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        WaitTrainTerminatedAction action = new WaitTrainTerminatedAction();
        Assert.assertEquals("name", Bundle.getMessage("WaitForTrainToTerminate"), action.getName());
    }
    
    @Test
    public void testIsMessageOkEnabled() {
        WaitTrainTerminatedAction action = new WaitTrainTerminatedAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }
    
    @Test
    public void testIsMessageFailEnabled() {
        WaitTrainTerminatedAction action = new WaitTrainTerminatedAction();
        Assert.assertTrue(action.isMessageFailEnabled());
    }
    
    @Test
    public void testIsConcurrentAction() {
        WaitTrainTerminatedAction action = new WaitTrainTerminatedAction();
        Assert.assertTrue(action.isConcurrentAction());
    }
    
    @Test
    public void testActionWaitForTrainToTerminate() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // confirm default
        Assert.assertTrue(train1.isBuildEnabled());

        WaitTrainTerminatedAction action = new WaitTrainTerminatedAction();
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

        // There are 3 locations in the route
        Assert.assertTrue(train1.build());
        train1.move();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        // next move is the last location
        train1.move();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
        
        // terminate train
        train1.move();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertTrue(automationItem.isActionSuccessful());
    }
    
    /**
     * Deselecting the build for a train should terminate the wait
     */
    @Test
    public void testDeselectBuild() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // confirm default
        Assert.assertTrue(train1.isBuildEnabled());

        WaitTrainTerminatedAction action = new WaitTrainTerminatedAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        automationItem.setTrain(train1);
        action.doAction();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        train1.setBuildEnabled(false);
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

        WaitTrainTerminatedAction action = new WaitTrainTerminatedAction();
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

    // private final static Logger log = LoggerFactory.getLogger(WaitTrainTerminatedActionTest.class);

}
