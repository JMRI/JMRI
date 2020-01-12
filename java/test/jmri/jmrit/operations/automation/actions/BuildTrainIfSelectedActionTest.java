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
public class BuildTrainIfSelectedActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        BuildTrainIfSelectedAction t = new BuildTrainIfSelectedAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        BuildTrainIfSelectedAction action = new BuildTrainIfSelectedAction();
        Assert.assertNotNull("exists",action);
        // does nothing, no automationItem
        action.doAction();
    }
    
    @Test
    public void testGetActionName() {
        BuildTrainIfSelectedAction action = new BuildTrainIfSelectedAction();
        Assert.assertEquals("name", Bundle.getMessage("BuildTrainIfSelected"), action.getName());
    }
    
    @Test
    public void testAction() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);
        
        BuildTrainIfSelectedAction action = new BuildTrainIfSelectedAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        // does nothing, no train assignment
        action.doAction();       
        Assert.assertFalse(train1.isBuilt());
        Assert.assertFalse(automationItem.isActionSuccessful());
        
        // reset the build enabled option
        train1.setBuildEnabled(false);
        automationItem.setTrain(train1);
        action.doAction();       
        Assert.assertFalse(train1.isBuilt());
        Assert.assertFalse(automationItem.isActionSuccessful());
        
        // allow train to be built
        train1.setBuildEnabled(true);
        automationItem.setTrain(train1);
        action.doAction();       
        Assert.assertTrue(train1.isBuilt());
        Assert.assertTrue(automationItem.isActionSuccessful());
        
        //try again
        action.doAction();
        Assert.assertFalse(automationItem.isActionSuccessful());
    }

    // private final static Logger log = LoggerFactory.getLogger(BuildTrainIfSelectedActionTest.class);

}
