package jmri.implementation;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NoFeedbackTurnoutOperatorTest {

    @Test
    public void testCTor() {
        Turnout it = (InstanceManager.getDefault(TurnoutManager.class)).provideTurnout("IT1");
        NoFeedbackTurnoutOperator t = new NoFeedbackTurnoutOperator((AbstractTurnout)it,5,5);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NoFeedbackTurnoutOperatorTest.class);

}
