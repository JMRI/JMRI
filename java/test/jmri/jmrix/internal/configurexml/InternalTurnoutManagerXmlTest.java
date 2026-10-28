package jmri.jmrix.internal.configurexml;

import jmri.CommonTurnoutOperation;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * InternalTurnoutManagerXmlTest.java
 *
 * Test for the InternalTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class InternalTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("InternalTurnoutManagerXml constructor",new InternalTurnoutManagerXml());
    }

    @Test
    public void testTurnoutSpecificOperationKeepsAutomationOnAfterReload() {
        // issue #15471 follow-up: a turnout's own automation copy must come back enabled after a restart
        JUnitUtil.initInternalTurnoutManager();
        Turnout to = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        to.setFeedbackMode(Turnout.DIRECT);
        to.setInhibitOperation(false);
        TurnoutOperation op = InstanceManager.getDefault(TurnoutOperationManager.class).getOperation("NoFeedback").makeNonce(to);
        ((CommonTurnoutOperation) op).setInterval(600);
        ((CommonTurnoutOperation) op).setMaxTries(3);
        to.setTurnoutOperation(op);

        Element stored = new InternalTurnoutManagerXml().store(InstanceManager.getDefault(TurnoutManager.class));
        Assert.assertNotNull("turnouts stored", stored);

        // start over with a fresh JMRI, as if restarted
        JUnitUtil.tearDown();
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();

        Assert.assertTrue("turnouts loaded", new InternalTurnoutManagerXml().load(stored, null));
        Turnout reloaded = InstanceManager.getDefault(TurnoutManager.class).getTurnout("IT1");
        Assert.assertNotNull("turnout reloaded", reloaded);
        Assert.assertFalse("automation still on after reload", reloaded.getInhibitOperation());
        TurnoutOperation reloadedOp = reloaded.getTurnoutOperation();
        Assert.assertNotNull("turnout has its automation", reloadedOp);
        Assert.assertTrue("still the turnout's own copy", reloadedOp.isNonce());
        Assert.assertEquals("interval kept", 600, ((CommonTurnoutOperation) reloadedOp).getInterval());
        Assert.assertEquals("times to try kept", 3, ((CommonTurnoutOperation) reloadedOp).getMaxTries());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

