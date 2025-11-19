package jmri.util;

import jmri.*;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
        assertTrue(sdm1 != sdm2);
    }


    @Test
    public void testInitInternalTurnoutManager() {
        assertFalse(InstanceManager.containsDefault(jmri.TurnoutManager.class));
        
        JUnitUtil.initInternalTurnoutManager();

        assertTrue(InstanceManager.containsDefault(jmri.TurnoutManager.class));
    }

    @Test
    public void testSetBeanStateAndWait() {
        JUnitUtil.initInternalTurnoutManager();
        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");

        JUnitUtil.setBeanStateAndWait(t, Turnout.THROWN);
        
        assertEquals(Turnout.THROWN, t.getCommandedState());
    }

    @Test
    public void testWaitForTextNotInvoked() {
        JUnitUtil.waitFor( () -> true, () -> "Should not call failure method " + failTest());
    }

    private String failTest() {
        fail("Method should not have been invoked");
        return "Should have failed Test if invoked";
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();     
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JUnitUtilTest.class);
}
