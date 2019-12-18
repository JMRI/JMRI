package jmri.jmrit.operations.automation.actions;

import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.automation.AutomationManager;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class StopAutomationActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        StopAutomationAction t = new StopAutomationAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        StopAutomationAction action = new StopAutomationAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        StopAutomationAction action = new StopAutomationAction();
        Assert.assertEquals("name", Bundle.getMessage("StopAutomation"), action.getName());
    }

    @Test
    public void testIsMessageOkEnabled() {
        StopAutomationAction action = new StopAutomationAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }
    
    @Test
    public void testIsMessageFailedEnabled() {
        StopAutomationAction action = new StopAutomationAction();
        Assert.assertTrue(action.isMessageFailEnabled());
    }
    
    @Test
    public void testGetComboBox() {
        StopAutomationAction action = new StopAutomationAction();
        Assert.assertNotNull("exists", action);
        JComboBox<Automation> cb = action.getComboBox();
        Assert.assertNull(cb);
        
        AutomationManager autoManager = InstanceManager.getDefault(AutomationManager.class);
        Automation test = autoManager.newAutomation("TEST_AUTOMATION");
        
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        cb = action.getComboBox();
        Assert.assertNotNull(cb);
        
        Assert.assertEquals("confirm", "TEST_AUTOMATION", cb.getItemAt(1).getName());
        Assert.assertEquals("confirm", test, cb.getItemAt(1));
    }

    @Test
    public void testNoAutomationSelected() {
        StopAutomationAction action = new StopAutomationAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
    }
    
    @Test
    public void testAction() {
        StopAutomationAction action = new StopAutomationAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        AutomationManager autoManager = InstanceManager.getDefault(AutomationManager.class);
        Automation test = autoManager.newAutomation("TEST_AUTOMATION");
        
        automationItem.setAutomationToRun(test);
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertTrue(automationItem.isActionSuccessful());
    }

    // private final static Logger log = LoggerFactory.getLogger(StopAutomationActionTest.class);

}
