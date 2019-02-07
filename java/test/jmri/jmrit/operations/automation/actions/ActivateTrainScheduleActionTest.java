package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dan Boudreau Copyright (C) 2018	
 */
public class ActivateTrainScheduleActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ActivateTrainScheduleAction t = new ActivateTrainScheduleAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        ActivateTrainScheduleAction action = new ActivateTrainScheduleAction();
        Assert.assertNotNull("exists",action);
        // does nothing, no automationItem
        action.doAction();
    }
    
    @Test
    public void testGetActionName() {
        ActivateTrainScheduleAction action = new ActivateTrainScheduleAction();
        Assert.assertEquals("name", Bundle.getMessage("ActivateTrainSchedule"), action.getName());
    }
    
    @Test
    public void testAction() {
        // confirm default
        TrainScheduleManager tsm = InstanceManager.getDefault(TrainScheduleManager.class);
        Assert.assertEquals("default schedule", "", tsm.getTrainScheduleActiveId());
        // load default schedules, 7 days of the week
        Assert.assertEquals("default number of schedules", 7, tsm.getSchedulesByNameList().size());
        Assert.assertNotNull("Monday exists", tsm.getScheduleByName("Monday"));
        
        ActivateTrainScheduleAction action = new ActivateTrainScheduleAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);       
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        // set "Monday" as the active 
        automationItem.setTrainSchedule(tsm.getScheduleByName("Monday"));
        
        String message = action.getActionString();
        Assert.assertTrue("message contains train schedule name", message.contains("Monday"));
        
        automationItem.doAction();
        // confirm change
        Assert.assertEquals("active schedule", tsm.getScheduleByName("Monday").getId(), tsm.getTrainScheduleActiveId());
        
        // confirm combobox is correctly selected
        Assert.assertEquals("selection", tsm.getScheduleByName("Monday"), action.getComboBox().getSelectedItem());
    }
    
    @Test
    public void testGetCombobox() {
        ActivateTrainScheduleAction action = new ActivateTrainScheduleAction();
        Assert.assertNotNull("exists",action);
        // 7 days of the week, plus null at start
        Assert.assertEquals("default schedules", 8, action.getComboBox().getItemCount());
    }

    // private final static Logger log = LoggerFactory.getLogger(ActivateTrainScheduleActionTest.class);

}
