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
public class MoveTrainActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        MoveTrainAction t = new MoveTrainAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        MoveTrainAction action = new MoveTrainAction();
        Assert.assertNotNull("exists",action);
        // does nothing, no automationItem
        action.doAction();
    }
    
    @Test
    public void testGetActionName() {
        MoveTrainAction action = new MoveTrainAction();
        Assert.assertEquals("name", Bundle.getMessage("MoveTrain"), action.getName());
    }
    
    @Test
    public void testTrainNotBuilt() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);
        
        MoveTrainAction action = new MoveTrainAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        // does nothing, no train assignment
        action.doAction();       
        Assert.assertFalse(train1.isTrainEnRoute());
        Assert.assertFalse(automationItem.isActionSuccessful());
        
        // does nothing, train not built
        automationItem.setTrain(train1);
        action.doAction();       
        Assert.assertFalse(train1.isTrainEnRoute());
        Assert.assertFalse(automationItem.isActionSuccessful());
    }
    
    @Test
    public void testAction() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);
        Assert.assertTrue(train1.build());
        
        MoveTrainAction action = new MoveTrainAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        // does nothing, no train assignment
        action.doAction();       
        Assert.assertFalse(train1.isTrainEnRoute());
        Assert.assertFalse(automationItem.isActionSuccessful());
        
        automationItem.setTrain(train1);
        action.doAction();       
        Assert.assertTrue(train1.isTrainEnRoute());
        Assert.assertTrue(automationItem.isActionSuccessful());
        
        //try again
        action.doAction();
        Assert.assertTrue(automationItem.isActionSuccessful());
    }
    
    @Test
    public void testActionRouteLocataion() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);
        Assert.assertTrue(train1.build());
        
        MoveTrainAction action = new MoveTrainAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        // does nothing, no train assignment
        action.doAction();       
        Assert.assertFalse(train1.isTrainEnRoute());
        Assert.assertFalse(automationItem.isActionSuccessful());
        
        automationItem.setTrain(train1);
        automationItem.setRouteLocation(train1.getTrainTerminatesRouteLocation());
        action.doAction();       
        Assert.assertTrue(train1.isTrainEnRoute());
        Assert.assertTrue(automationItem.isActionSuccessful());
    }

    // private final static Logger log = LoggerFactory.getLogger(MoveTrainActionTest.class);

}
