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
public class DeselectTrainActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        DeselectTrainAction t = new DeselectTrainAction();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testActionNoAutomationItem() {
        DeselectTrainAction action = new DeselectTrainAction();
        Assert.assertNotNull("exists",action);
        // does nothing, no automationItem
        action.doAction();
    }
    
    @Test
    public void testGetActionName() {
        DeselectTrainAction action = new DeselectTrainAction();
        Assert.assertEquals("name", Bundle.getMessage("DeselectTrain"), action.getName());
    }
    
    @Test
    public void testAction() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);
        
        // setup action
        DeselectTrainAction action = new DeselectTrainAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        // does nothing, no train assignment
        action.doAction();       
        Assert.assertTrue(train1.isBuildEnabled());
        Assert.assertFalse(automationItem.isActionSuccessful());
        
        automationItem.setTrain(train1);
        action.doAction();       
        Assert.assertFalse(train1.isBuildEnabled());
        Assert.assertTrue(automationItem.isActionSuccessful());
    }

    // private final static Logger log = LoggerFactory.getLogger(DeselectTrainActionTest.class);

}
