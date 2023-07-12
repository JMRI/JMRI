package jmri.jmrit.operations.automation.actions;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.AutomationItem;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CounterActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        CounterAction t = new CounterAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testGetActionName() {
        CounterAction action = new CounterAction();
        Assert.assertEquals("name", Bundle.getMessage("Counter"), action.getName());
    }
    
    @Test
    public void testAction() {
        // setup action
        CounterAction action = new CounterAction();
        Assert.assertNotNull("exists",action);
        AutomationItem automationItem = new AutomationItem("TestId");
        automationItem.setAction(action);
        Assert.assertEquals("confirm registered", automationItem, action.getAutomationItem());
        
        action.doAction();
        Assert.assertEquals("confirm counter incremented", "1", action.getActionSuccessfulString());
        action.doAction();
        Assert.assertEquals("confirm counter incremented", "2", automationItem.getStatus());
    }

    // private final static Logger log = LoggerFactory.getLogger(HaltActionTest.class);

}
