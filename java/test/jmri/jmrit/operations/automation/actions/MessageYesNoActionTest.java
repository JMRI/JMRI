package jmri.jmrit.operations.automation.actions;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MessageYesNoActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        MessageYesNoAction t = new MessageYesNoAction();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testActionNoAutomationItem() {
        MessageYesNoAction action = new MessageYesNoAction();
        Assert.assertNotNull("exists", action);
        // does nothing, no automationItem
        action.doAction();
    }

    @Test
    public void testGetActionName() {
        MessageYesNoAction action = new MessageYesNoAction();
        Assert.assertEquals("name", Bundle.getMessage("MessageYesNo"), action.getName());
    }
    
    @Test
    public void testIsMessageOkEnabled() {
        MessageYesNoAction action = new MessageYesNoAction();
        Assert.assertTrue(action.isMessageOkEnabled());
    }
    
    @Test
    public void testGetActionSuccessfulString() {
        MessageYesNoAction action = new MessageYesNoAction();
        Assert.assertEquals("Confirm", Bundle.getMessage("ButtonYes"), action.getActionSuccessfulString());
    }
    
    @Test
    public void testGetActionFailedString() {
        MessageYesNoAction action = new MessageYesNoAction();
        Assert.assertEquals("Confirm", Bundle.getMessage("ButtonNo"), action.getActionFailedString());
    }
    
    @Test
    public void testNoMessage() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        MessageYesNoAction action = new MessageYesNoAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());

        // no message, so no pop up
        action.doAction();
        Assert.assertFalse(automationItem.isActionRunning());
    }
    
    @Test
    public void testYesAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        MessageYesNoAction action = new MessageYesNoAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        automationItem.setMessage("TEST_MESSAGE");

        // Yes or No dialogue should appear
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
        Assert.assertTrue(automationItem.isActionRunning());
        
        String title = automationItem.getId() + "  " + action.getActionString();
        JemmyUtil.pressDialogButton(title, Bundle.getMessage("ButtonYes"));
        
        try {
            doAction.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertTrue(automationItem.isActionSuccessful());
    }
    
    @Test
    public void testNoAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        MessageYesNoAction action = new MessageYesNoAction();
        Assert.assertNotNull("exists", action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        automationItem.setMessage("TEST_MESSAGE");

        // Yes or No dialogue should appear
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
        Assert.assertTrue(automationItem.isActionRunning());
        
        String title = automationItem.getId() + "  " + action.getActionString();
        JemmyUtil.pressDialogButton(title, Bundle.getMessage("ButtonNo"));
        
        try {
            doAction.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Assert.assertFalse(automationItem.isActionRunning());
        Assert.assertFalse(automationItem.isActionSuccessful());
    }

    // private final static Logger log = LoggerFactory.getLogger(MessageYesNoActionTest.class);

}
