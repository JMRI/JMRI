package jmri.jmrit.operations.automation.actions;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
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
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
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
    
    @Test
    public void testActionMessages() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);
        
        BuildTrainAction action = new BuildTrainAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        automationItem.setTrain(train1);
        automationItem.setMessage("Show this message when successful");
        automationItem.setMessageFail("Show this message when fail");
        
        // should cause dialog to appear
        Thread doAction = new Thread(new Runnable() {
            @Override
            public void run() {
                action.doAction();
            }
        });
        doAction.setName("Do Action"); // NOI18N
        doAction.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return doAction.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");
        
        String title = automationItem.getId() + "  " + action.getActionString();
        JemmyUtil.pressDialogButton(title, Bundle.getMessage("ButtonOK"));
        
        Assert.assertTrue(train1.isBuilt());
        Assert.assertTrue(automationItem.isActionSuccessful());
        
        //try again
        // should dialog to appear
        Thread doAction2 = new Thread(new Runnable() {
            @Override
            public void run() {
                action.doAction();
            }
        });
        doAction2.setName("Do Action 2"); // NOI18N
        doAction2.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return doAction2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");
        
        title = automationItem.getId() + " " + Bundle.getMessage("Failed") + " " + action.getActionString();
        JemmyUtil.pressDialogButton(title, Bundle.getMessage("Halt"));
        
        Assert.assertFalse(automationItem.isActionSuccessful());
    }

    // private final static Logger log = LoggerFactory.getLogger(BuildTrainActionTest.class);

}
