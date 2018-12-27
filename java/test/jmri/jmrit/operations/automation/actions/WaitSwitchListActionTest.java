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
public class WaitSwitchListActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        WaitSwitchListAction t = new WaitSwitchListAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        WaitSwitchListAction action = new WaitSwitchListAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        WaitSwitchListAction action = new WaitSwitchListAction();
        Assert.assertEquals("name", Bundle.getMessage("WaitForSwitchListChange"), action.getName());
    }
    
    @Test
    public void testIsMessageOkEnabled() {
        WaitSwitchListAction action = new WaitSwitchListAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }
    
    @Test
    public void testIsMessageFailEnabled() {
        WaitSwitchListAction action = new WaitSwitchListAction();
        Assert.assertFalse(action.isMessageFailEnabled());
    }
    
    @Test
    public void testIsConcurrentAction() {
        WaitSwitchListAction action = new WaitSwitchListAction();
        Assert.assertTrue(action.isConcurrentAction());
    }
    
    @Test
    public void testCancel() {
        JUnitOperationsUtil.initOperationsData();

        WaitSwitchListAction action = new WaitSwitchListAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        action.doAction();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());

        // cancel
        action.cancelAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
    }
    
    @Test
    public void testWaitSwitchListAction() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // confirm default
        Assert.assertTrue(train1.isBuildEnabled());

        WaitSwitchListAction action = new WaitSwitchListAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        // start wait
        action.doAction();
        Assert.assertTrue(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
        
        // build train
        Assert.assertTrue(train1.build());

        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertTrue(automationItem.isActionSuccessful());
    }

    // private final static Logger log = LoggerFactory.getLogger(WaitSwitchListActionTest.class);

}
