package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.trains.timetable.TrainScheduleManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dan Boudreau Copyright (C) 2018	
 */
public class ActivateTimetableActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ActivateTimetableAction t = new ActivateTimetableAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        ActivateTimetableAction action = new ActivateTimetableAction();
        Assert.assertNotNull("exists",action);
        // does nothing, no automationItem
        action.doAction();
    }
    
    @Test
    public void testGetActionName() {
        ActivateTimetableAction action = new ActivateTimetableAction();
        Assert.assertEquals("name", Bundle.getMessage("ActivateTimetable"), action.getName());
    }
    
    @Test
    public void testAction() {
        // confirm default
        TrainScheduleManager tsm = InstanceManager.getDefault(TrainScheduleManager.class);
        Assert.assertEquals("default schedule", "", tsm.getTrainScheduleActiveId());
        // load default schedules, 7 days of the week
        Assert.assertEquals("default number of schedules", 7, tsm.getSchedulesByNameList().size());
        Assert.assertNotNull("Monday exists", tsm.getScheduleByName("Monday"));
        
        ActivateTimetableAction action = new ActivateTimetableAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        action.setAutomationItem(automationItem);
        
        // set "Monday" as the active 
        automationItem.setTrainSchedule(tsm.getScheduleByName("Monday"));
        action.doAction();
        // confirm change
        Assert.assertEquals("active schedule", tsm.getScheduleByName("Monday").getId(), tsm.getTrainScheduleActiveId());
        
        // confirm combobox is correctly selected
        Assert.assertEquals("selection", tsm.getScheduleByName("Monday"), action.getComboBox().getSelectedItem());
    }
    
    @Test
    public void testGetCombobox() {
        ActivateTimetableAction action = new ActivateTimetableAction();
        Assert.assertNotNull("exists",action);
        // 7 days of the week, plus null at start
        Assert.assertEquals("default schedules", 8, action.getComboBox().getItemCount());
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

    // private final static Logger log = LoggerFactory.getLogger(ActivateTimetableActionTest.class);

}
