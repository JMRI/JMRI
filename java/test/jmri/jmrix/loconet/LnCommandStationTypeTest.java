package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.loconet.LnSensor class.
 *
 * @author Bob Jacobsen Copyright 2001, 2002
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
    public void checkNumberOfWorkstations() {
        // if you need to change this, you need to add lines below in the tests
       Assert.assertEquals("Number Of workstations to test",LnCommandStationType.values().length,20);
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
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_DCS210PLUS.getImplementsIdle());

        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR2_ALONE.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR3_ALONE.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR4_ALONE.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_USB_DCS240_ALONE.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_USB_DCS52_ALONE.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_STANDALONE.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_LBPS.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_IBX_TYPE_1.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_IBX_TYPE_2.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_LBPS.getImplementsIdle());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_MM.getImplementsIdle());
        }

    @Test
    public void testGetProgPowersOff() {
        //DB150 only one that needs power turned back on after programmin
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_DB150.getProgPowersOff());
        // all others false
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS050.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS051.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS100.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS200.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS210.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS240.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR2_ALONE.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR3_ALONE.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR4_ALONE.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_USB_DCS240_ALONE.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_USB_DCS52_ALONE.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_STANDALONE.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_LBPS.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_IBX_TYPE_1.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_IBX_TYPE_2.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_LBPS.getProgPowersOff());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_MM.getProgPowersOff());
    }

    @Test
    public void testGetSupportsMultimeter() {
        //DCS Evolution series supports MultiMeter
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_DCS052.getSupportsMultimeter());
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_DCS210.getSupportsMultimeter());
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_DCS240.getSupportsMultimeter());
        // may be not true, but no harm
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_USB_DCS240_ALONE.getSupportsMultimeter());
        Assert.assertTrue(LnCommandStationType.COMMAND_STATION_USB_DCS52_ALONE.getSupportsMultimeter());
        // all others false
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DB150.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS050.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS051.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS100.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_DCS200.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR2_ALONE.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR3_ALONE.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR4_ALONE.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_STANDALONE.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_IBX_TYPE_1.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_IBX_TYPE_2.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_LBPS.getSupportsMultimeter());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_MM.getSupportsMultimeter());
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
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_PR4_ALONE.getSupportsLocoReset());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_USB_DCS240_ALONE.getSupportsLocoReset());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_USB_DCS52_ALONE.getSupportsLocoReset());
        Assert.assertFalse(LnCommandStationType.COMMAND_STATION_STANDALONE.getSupportsLocoReset());

        // the following command station types are _assumed_ by the coder to not support "idle".
        // these assertions need to be verified with respect to real hardware.
        Assert.assertFalse("IBX Type 1 loco reset expect false", LnCommandStationType.COMMAND_STATION_IBX_TYPE_1.getSupportsLocoReset());
        Assert.assertFalse("IBX Type 2 loco reset expect false", LnCommandStationType.COMMAND_STATION_IBX_TYPE_2.getSupportsLocoReset());
        Assert.assertFalse("LBPS loco reset expect false", LnCommandStationType.COMMAND_STATION_LBPS.getSupportsLocoReset());
        Assert.assertFalse("MM loco reset expect false", LnCommandStationType.COMMAND_STATION_MM.getSupportsLocoReset());
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
