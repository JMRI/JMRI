package jmri.jmrit.operations.automation.actions;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ResetSwitchListsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ResetSwitchListsAction t = new ResetSwitchListsAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        ResetSwitchListsAction action = new ResetSwitchListsAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        ResetSwitchListsAction action = new ResetSwitchListsAction();
        Assert.assertEquals("name", Bundle.getMessage("ResetSwitchLists"), action.getName());
    }

    @Test
    public void testIsMessageOkEnabled() {
        ResetSwitchListsAction action = new ResetSwitchListsAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }

//    @Test
//    public void testAction() {
//        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        JUnitOperationsUtil.initOperationsData();
//        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
//        JUnitOperationsUtil.checkOperationsShutDownTask();
//
//    }

    // private final static Logger log = LoggerFactory.getLogger(UpdateSwitchListActionTest.class);

}
