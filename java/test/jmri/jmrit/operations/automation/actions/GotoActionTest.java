package jmri.jmrit.operations.automation.actions;

import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.automation.AutomationManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class GotoActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        GotoAction t = new GotoAction();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testActionNoAutomationItem() {
        GotoAction action = new GotoAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        GotoAction action = new GotoAction();
        Assert.assertEquals("name", Bundle.getMessage("Goto"), action.getName());
    }

    @Test
    public void testActionGetSuccessfulString() {
        // this test needs an automation
        Automation automation =
                InstanceManager.getDefault(AutomationManager.class).newAutomation("Test_Goto_Automation");

        GotoAction action = new GotoAction();
        Assert.assertNotNull("exists", action);

        AutomationItem automationItem = automation.addItem();
        automationItem.setAction(action);

        AutomationItem automationItem2 = automation.addItem();
        automationItem.setGotoAutomationItem(automationItem2);

        String s = action.getActionSuccessfulString();
        Assert.assertTrue(s.contains(Bundle.getMessage("ButtonOK")));
        Assert.assertFalse(s.contains(" -> "));

        // now get the branched success string
        automationItem.setGotoBranched(true);
        s = action.getActionSuccessfulString();
        Assert.assertTrue(s.contains(Bundle.getMessage("ButtonOK")));
        Assert.assertTrue(s.contains(" -> "));
        Assert.assertTrue(s.contains(automationItem2.getId()));
    }

    @Test
    public void testGetJComboBox() {
        GotoAction action = new GotoAction();
        Assert.assertNotNull("exists", action);

        // create some automation items to jump to
        Automation automation = JUnitOperationsUtil.createAutomation();
        AutomationItem automationItem = automation.addItem();
        automationItem.setAction(action);

        automationItem.setGotoAutomationItem(automation.getItemsBySequenceList().get(2));

        JComboBox<AutomationItem> box = action.getComboBox();
        Assert.assertEquals("Selected", automation.getItemsBySequenceList().get(2), box.getSelectedItem());
        Assert.assertEquals("Number of automationItems", 5, box.getItemCount());

        // confirm that goto to this automationItem isn't included in the list
        for (int i = 0; i < box.getItemCount(); i++) {
            Assert.assertNotEquals("this automation item", box.getItemAt(i), automationItem);
        }
    }

    @Test
    public void testAction() {
        // this test needs an automation
        Automation automation =
                InstanceManager.getDefault(AutomationManager.class).newAutomation("Test_Goto_Automation");
        GotoAction action = new GotoAction();
        Assert.assertNotNull("exists", action);

        AutomationItem automationItem1 = automation.addItem();
        automationItem1.setAction(action);
        Assert.assertEquals("confirm registered", automationItem1, action.getAutomationItem());

        // fails, no goto assignment
        action.doAction();
        Assert.assertFalse(automationItem1.isActionSuccessful());
        Assert.assertEquals("current automation item", automationItem1, automation.getCurrentAutomationItem());

        // provide actionItem to jump to
        AutomationItem automationItem2 = automation.addItem();
        automationItem2.setAction(new HaltAction());

        // and one more after the halt
        AutomationItem automationItem3 = automation.addItem();
        automationItem2.setAction(new HaltAction());

        automationItem1.setGotoAutomationItem(automationItem2);

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

        Thread run2 = JUnitUtil.getThreadByName("Run action item: " + automationItem2.getId());
        
        if (run2 != null) {
            try {
                run2.join();
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        // the first halt
        Assert.assertTrue(automationItem2.isActionSuccessful());
        Assert.assertTrue(automationItem2.isActionRan());
        Assert.assertFalse(automationItem2.isActionRunning());

        // the next item to run is the 2nd halt
        Assert.assertNotEquals("current automation item", automationItem1, automation.getCurrentAutomationItem());
        Assert.assertEquals("current automation item", automationItem3, automation.getCurrentAutomationItem());
        Assert.assertEquals("last automation item", automationItem2, automation.getLastAutomationItem());

    }

    // private final static Logger log = LoggerFactory.getLogger(GotoActionTest.class);

}
