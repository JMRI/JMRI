package jmri.util;

import jmri.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.util.JUnitUtil itself.  
 * We don't normally test the test code per se,
 * but this is so commonly used that it seems wise to 
 * confirm some behaviors.
 *
 * @author Bob Jacobsen Copyright 2019
 */
public class JUnitUtilTest {


    @Test
    public void testInstanceManagerReset() {
        ShutDownManager sdm1 = InstanceManager.getDefault(ShutDownManager.class);

        JUnitUtil.resetInstanceManager();

        ShutDownManager sdm2 = InstanceManager.getDefault(ShutDownManager.class);
        Assert.assertTrue(sdm1 != sdm2);
    }


    @Test
    public void testInitInternalTurnoutManager() {
        Assert.assertFalse(InstanceManager.containsDefault(jmri.TurnoutManager.class));
        
        JUnitUtil.initInternalTurnoutManager();

        Assert.assertTrue(InstanceManager.containsDefault(jmri.TurnoutManager.class));
    }

    @Test
    public void testSetBeanStateAndWait() {
        JUnitUtil.initInternalTurnoutManager();
        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");

        JUnitUtil.setBeanStateAndWait(t, Turnout.THROWN);
        
        Assert.assertEquals(Turnout.THROWN, t.getCommandedState());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();     
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JUnitUtilTest.class);
}
