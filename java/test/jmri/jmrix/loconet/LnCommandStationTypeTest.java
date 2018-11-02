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
        memo.dispose();
    }

    @Test
    public void testGetImpelementsIdle() {
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS050.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS051.getImplementsIdle());
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_DCS100.getImplementsIdle());
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_DB150.getImplementsIdle());
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_DCS200.getImplementsIdle());
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_DCS210.getImplementsIdle());
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_DCS240.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR2_ALONE.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR3_ALONE.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_STANDALONE.getImplementsIdle());

        // the following command station types are _assumed_ by the coder to not support "idle".
        // these assertions need to be verified with respect to real hardware.
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_IBX_TYPE_1.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_IBX_TYPE_2.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_LBPS.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_MM.getImplementsIdle());
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
