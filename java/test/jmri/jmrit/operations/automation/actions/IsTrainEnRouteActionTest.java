package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IsTrainEnRouteActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        IsTrainEnRouteAction t = new IsTrainEnRouteAction();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testActionNoAutomationItem() {
        IsTrainEnRouteAction action = new IsTrainEnRouteAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        IsTrainEnRouteAction action = new IsTrainEnRouteAction();
        Assert.assertEquals("name", Bundle.getMessage("IsTrainEnRoute"), action.getName());
    }

    @Test
    public void testAction() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        IsTrainEnRouteAction action = new IsTrainEnRouteAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        // does nothing, no train assignment
        action.doAction();
        Assert.assertFalse(train1.isTrainEnRoute());
        Assert.assertFalse(automationItem.isActionSuccessful());

        // train is not built
        automationItem.setTrain(train1);
        action.doAction();
        Assert.assertFalse(train1.isTrainEnRoute());
        Assert.assertFalse(automationItem.isActionSuccessful());

        Assert.assertTrue(train1.build());

        // train is not en-route
        automationItem.setTrain(train1);
        action.doAction();
        Assert.assertFalse(train1.isTrainEnRoute());
        Assert.assertFalse(automationItem.isActionSuccessful());

        train1.move();
        Assert.assertTrue(train1.isTrainEnRoute());

        action.doAction();
        Assert.assertTrue(automationItem.isActionSuccessful());
    }

    @Test
    public void testActionSelectRoutelocation() {
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        IsTrainEnRouteAction action = new IsTrainEnRouteAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        Assert.assertTrue(train1.build());

        // select second location in the train's route
        automationItem.setTrain(train1);
        RouteLocation rl = train1.getRoute().getRouteLocationBySequenceNumber(2);
        Assert.assertNotNull("exists", rl);

        automationItem.setRouteLocation(rl);
        action.doAction();
        // train status en-route when it departs, not the same as the action status
        Assert.assertFalse(train1.isTrainEnRoute());
        // train hasn't reached the route location
        Assert.assertTrue(automationItem.isActionSuccessful());

        train1.move();
        Assert.assertTrue(train1.isTrainEnRoute());

        action.doAction();
        // train at the route location
        Assert.assertFalse(automationItem.isActionSuccessful());

        train1.move();
        Assert.assertTrue(train1.isTrainEnRoute());

        action.doAction();
        // train has departed the route location
        Assert.assertFalse(automationItem.isActionSuccessful());
    }

    // private final static Logger log = LoggerFactory.getLogger(IsTrainEnRouteActionTest.class);

}
