package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.timetable.TrainSchedule;
import jmri.jmrit.operations.trains.timetable.TrainScheduleManager;
import jmri.util.JUnitOperationsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ApplyTimetableActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ApplyTimetableAction t = new ApplyTimetableAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        ApplyTimetableAction action = new ApplyTimetableAction();
        Assert.assertNotNull("exists",action);
        // does nothing, no automationItem
        action.doAction();
    }
    
    @Test
    public void testGetActionName() {
        ApplyTimetableAction action = new ApplyTimetableAction();
        Assert.assertEquals("name", Bundle.getMessage("ApplyTimetable"), action.getName());
    }
    
    /**
     * The action will set or reset the train build enabled option
     */
    @Test
    public void testAction() {
        JUnitOperationsUtil.initOperationsData();
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainByName("STF");
        Assert.assertNotNull("train exists", train1);
        Train train2 = InstanceManager.getDefault(TrainManager.class).getTrainByName("SFF");
        Assert.assertNotNull("train exists", train2);
        
        // confirm default
        TrainScheduleManager tsm = InstanceManager.getDefault(TrainScheduleManager.class);
        Assert.assertEquals("default schedule", "", tsm.getTrainScheduleActiveId());
        // load default schedules, 7 days of the week
        Assert.assertEquals("default number of schedules", 7, tsm.getSchedulesByNameList().size());
        TrainSchedule ts = tsm.getScheduleByName("Friday");
        Assert.assertNotNull("Friday exists", ts);
        
        // modify and make "Friday" the active schedule
        tsm.setTrainScheduleActiveId(ts.getId());
        Assert.assertFalse(ts.containsTrainId(train1.getId()));
        Assert.assertFalse(ts.containsTrainId(train2.getId()));
        // enable train build
        ts.addTrainId(train2.getId());
        
        // setup action
        ApplyTimetableAction action = new ApplyTimetableAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        action.setAutomationItem(automationItem);
        
        // confirm default and modify train 2
        Assert.assertTrue(train1.isBuildEnabled());
        Assert.assertTrue(train2.isBuildEnabled());
        train2.setBuildEnabled(false);
        Assert.assertFalse(train2.isBuildEnabled());
        
        action.doAction();
        Assert.assertFalse(train1.isBuildEnabled());
        Assert.assertTrue(train2.isBuildEnabled());
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

    // private final static Logger log = LoggerFactory.getLogger(ApplyTimetableActionTest.class);

}
