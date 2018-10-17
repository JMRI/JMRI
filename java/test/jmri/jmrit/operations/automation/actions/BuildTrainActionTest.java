package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class BuildTrainActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        BuildTrainAction t = new BuildTrainAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        BuildTrainAction action = new BuildTrainAction();
        Assert.assertNotNull("exists",action);
        // does nothing, no automationItem
        action.doAction();
    }
    
    @Test
    public void testGetActionName() {
        BuildTrainAction action = new BuildTrainAction();
        Assert.assertEquals("name", Bundle.getMessage("BuildTrain"), action.getName());
    }
    
    @Test
    public void testAction() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);
        
        BuildTrainAction action = new BuildTrainAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        action.setAutomationItem(automationItem);
        
        // does nothing, no train assignment
        action.doAction();       
        Assert.assertFalse(train1.isBuilt());
        Assert.assertFalse(automationItem.isActionSuccessful());
        
        automationItem.setTrain(train1);
        action.doAction();       
        Assert.assertTrue(train1.isBuilt());
        Assert.assertTrue(automationItem.isActionSuccessful());
        
        //try again
        action.doAction();
        Assert.assertFalse(automationItem.isActionSuccessful());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BuildTrainActionTest.class);

}
