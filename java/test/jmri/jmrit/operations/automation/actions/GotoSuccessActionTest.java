package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.automation.AutomationManager;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class GotoSuccessActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        GotoSuccessAction t = new GotoSuccessAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testActionNoAutomationItem() {
        GotoSuccessAction action = new GotoSuccessAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        GotoSuccessAction action = new GotoSuccessAction();
        Assert.assertEquals("name", Bundle.getMessage("GotoIfSuccess"), action.getName());
    }

    /**
     * Test conditional goto. A series of halts is assigned after the goto.
     */
    @Test
    public void testAction() {
        // this test needs an automation
        Automation automation =
                InstanceManager.getDefault(AutomationManager.class).newAutomation("Test_Goto_Automation");
        GotoSuccessAction action = new GotoSuccessAction();
        Assert.assertNotNull("exists", action);

        AutomationItem automationItem1 = automation.addItem();
        automationItem1.setAction(action);
        Assert.assertEquals("confirm registered", automationItem1, action.getAutomationItem());

        // fails, no goto assignment
        action.doAction();
        Assert.assertFalse(automationItem1.isActionSuccessful());
        Assert.assertEquals("current automation item", automationItem1, automation.getCurrentAutomationItem());

        AutomationItem automationItem2 = automation.addItem();
        automationItem2.setAction(new HaltAction());

        AutomationItem automationItem3 = automation.addItem();
        automationItem3.setAction(new HaltAction());
        
        AutomationItem automationItem4 = automation.addItem();
        automationItem4.setAction(new HaltAction());

        // goto this item
        automationItem1.setGotoAutomationItem(automationItem3);

        automation.run();

        Thread run = JUnitUtil.getThreadByName("Run action item: " + automationItem1.getId());
        
        if (run != null) {
            try {
                run.join();
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        Assert.assertTrue(automationItem1.isActionSuccessful());
        Assert.assertTrue(automationItem1.isActionRan());
        Assert.assertFalse(automationItem1.isActionRunning());

        // this test does not branch
        Thread run2 = JUnitUtil.getThreadByName("Run action item: " + automationItem2.getId());

        if (run2 != null) {
            try {
                run2.join();
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        
        // the first halt is executed
        Assert.assertTrue(automationItem2.isActionSuccessful());
        Assert.assertTrue(automationItem2.isActionRan());
        Assert.assertFalse(automationItem2.isActionRunning());
       
        // the 2nd halt is the next to execute
        Assert.assertFalse(automationItem3.isActionSuccessful());
        Assert.assertFalse(automationItem3.isActionRan());
        Assert.assertFalse(automationItem3.isActionRunning());

        // the next item to run is the 2nd halt
        Assert.assertNotEquals("current automation item", automationItem1, automation.getCurrentAutomationItem());
        Assert.assertEquals("current automation item", automationItem3, automation.getCurrentAutomationItem());
        Assert.assertEquals("last automation item", automationItem2, automation.getLastAutomationItem());
    }

    // private final static Logger log = LoggerFactory.getLogger(GotoSuccessActionTest.class);

}
