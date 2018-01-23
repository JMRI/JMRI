package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.LnSensor class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002
 */
public class LnCommandStationTypeTest {

    @Test
    public void testLnCommandStationTypeName() {
        Assert.assertEquals("DCS200", LnCommandStationType.COMMAND_STATION_DCS200.getName());
    }

    @Test
    public void testFind() {
        Assert.assertEquals(LnCommandStationType.COMMAND_STATION_DCS200, LnCommandStationType.getByName("DCS200"));
    }

    @Test
    public void testThrottleManager() {
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(null, new SlotManager(new LocoNetInterfaceScaffold()));

        jmri.ThrottleManager tm = LnCommandStationType.COMMAND_STATION_DCS200.getThrottleManager(memo);
        Assert.assertEquals(LnThrottleManager.class, tm.getClass());
        ((LnThrottleManager)tm).dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
