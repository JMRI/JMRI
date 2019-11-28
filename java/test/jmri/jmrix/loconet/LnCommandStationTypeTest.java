package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.*;

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

    @Test
    public void testSupportsLocoReset() {
        Assert.assertFalse("DCS050 loco reset expect false", LnCommandStationType.COMMAND_STATION_DCS050.getSupportsLocoReset());
        Assert.assertFalse("DCS051 loco reset expect false", LnCommandStationType.COMMAND_STATION_DCS051.getSupportsLocoReset());
        Assert.assertTrue("DCS052 loco reset expect true", LnCommandStationType.COMMAND_STATION_DCS052.getSupportsLocoReset());
        Assert.assertFalse("DCS100 loco reset expect false", LnCommandStationType.COMMAND_STATION_DCS100.getSupportsLocoReset());
        Assert.assertFalse("DB150 loco reset expect false", LnCommandStationType.COMMAND_STATION_DB150.getSupportsLocoReset());
        Assert.assertFalse("DCS200 loco reset expect false", LnCommandStationType.COMMAND_STATION_DCS200.getSupportsLocoReset());
        Assert.assertTrue("DCS210 loco reset expect true", LnCommandStationType.COMMAND_STATION_DCS210.getSupportsLocoReset());
        Assert.assertTrue("DCS242 loco reset expect true", LnCommandStationType.COMMAND_STATION_DCS240.getSupportsLocoReset());
        Assert.assertFalse("PR2 standalone loco reset expect false", LnCommandStationType.COMMAND_STATION_PR2_ALONE.getSupportsLocoReset());
        Assert.assertFalse("PR3 standalone loco reset expect false", LnCommandStationType.COMMAND_STATION_PR3_ALONE.getSupportsLocoReset());
        Assert.assertFalse("Standalone loco reset expect false", LnCommandStationType.COMMAND_STATION_STANDALONE.getSupportsLocoReset());

        // the following command station types are _assumed_ by the coder to not support "idle".
        // these assertions need to be verified with respect to real hardware.
        Assert.assertFalse("IBX Type 1 loco reset expect false", LnCommandStationType.COMMAND_STATION_IBX_TYPE_1.getSupportsLocoReset());
        Assert.assertFalse("IBX Type 2 loco reset expect false", LnCommandStationType.COMMAND_STATION_IBX_TYPE_2.getSupportsLocoReset());
        Assert.assertFalse("LBPS loco reset expect false", LnCommandStationType.COMMAND_STATION_LBPS.getSupportsLocoReset());
        Assert.assertFalse("MM loco reset expect false", LnCommandStationType.COMMAND_STATION_MM.getSupportsLocoReset());
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
