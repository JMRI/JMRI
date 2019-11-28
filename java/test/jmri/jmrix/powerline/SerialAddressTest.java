package jmri.jmrix.powerline;

import jmri.Manager.NameValidity;
import jmri.jmrix.powerline.simulator.SpecificSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * JUnit tests for the SerialAddress utility class.
 *
 * @author	Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2007, 2008
 */
public class SerialAddressTest {

    SerialTrafficControlScaffold tc = null;

    @Test
    public void testValidSystemNameFormat() {
        Assert.assertEquals("valid format - PLA1", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLA1", 'L'));
        Assert.assertEquals("valid format - PLA16", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLA16", 'L'));
        Assert.assertEquals("valid format - PLK3", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLK3", 'L'));
        Assert.assertEquals("valid format - PTA1", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTA1", 'T'));
        Assert.assertEquals("valid format - PTA16", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTA16", 'T'));
        Assert.assertEquals("valid format - PTK3", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTK3", 'T'));
        Assert.assertEquals("invalid format - PL2", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2", 'L'));
        Assert.assertEquals("invalid format - PL0B2", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL0B2", 'L'));
        Assert.assertEquals("invalid format - PL", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL", 'L'));
        Assert.assertEquals("valid format - PLB2", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLB2", 'L'));
        Assert.assertEquals("invalid format - PY2005", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PY2005", 'L'));
        Assert.assertEquals("invalid format - PY2B5", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PY2B5", 'L'));
        Assert.assertEquals("invalid format - PL22001", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL22001", 'L'));
        Assert.assertEquals("invalid format - PL22B1", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL22B1", 'L'));
        Assert.assertEquals("invalid format - PL22000", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL22000", 'L'));
        Assert.assertEquals("invalid format - PL22B0", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL22B0", 'L'));
        Assert.assertEquals("invalid format - PL2999", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2999", 'L'));
        Assert.assertEquals("invalid format - PL2B2048", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2B2048", 'L'));
        Assert.assertEquals("invalid format - PL2B2049", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2B2049", 'L'));
        Assert.assertEquals("invalid format - PL2B33", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2B33", 'L'));
        Assert.assertEquals("invalid format - PL127032", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL127032", 'L'));
        Assert.assertEquals("invalid format - PL127001", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL127001", 'L'));
        Assert.assertEquals("invalid format - PL127000", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL127000", 'L'));
        Assert.assertEquals("invalid format - PL127B7", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL127B7", 'L'));
        Assert.assertEquals("invalid format - PL128B7", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL128B7", 'L'));
        Assert.assertEquals("invalid format - PL2oo5", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2oo5", 'L'));
        Assert.assertEquals("invalid format - PL2aB5", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2aB5", 'L'));
        Assert.assertEquals("invalid format - PL2B5x", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2B5x", 'L'));
    }

    @Test
    public void testValidSystemNameConfig() {
        Assert.assertTrue("valid config PLA1", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PLA1", 'L'));
        Assert.assertTrue("valid config PLB7", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PLB7", 'L'));
        Assert.assertFalse("invalid config PL4007", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PL4007", 'L'));
        Assert.assertFalse("invalid config PL10033", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PL10033", 'L'));
        Assert.assertTrue("valid config PSK16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PSK16", 'S'));
        Assert.assertTrue("valid config PSP16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PSP16", 'S'));
        Assert.assertTrue("valid config PTK16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PTK16", 'T'));
        Assert.assertTrue("valid config PTP16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PTP16", 'T'));
    }

    @Test
    public void testIsInsteonTrue() {
        Assert.assertTrue("PL01.02.03", tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.02.03"));
        Assert.assertTrue("PLA1.02.03", tc.getAdapterMemo().getSerialAddress().isInsteon("PLA1.02.03"));
        Assert.assertTrue("PLA1.A2.03", tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.A2.03"));
        Assert.assertTrue("PLA1.02.A3", tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.02.A3"));
    }

    @Test
    public void testIsInsteonFalse() {
        Assert.assertFalse("PLA1", tc.getAdapterMemo().getSerialAddress().isInsteon("PLA1"));
    }

    @Test
    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize PLB7", "PLB7", tc.getAdapterMemo().getSerialAddress().normalizeSystemName("PLB7"));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();        
        SpecificSystemConnectionMemo memo = new SpecificSystemConnectionMemo();
        // prepare an interface, register
        tc = new SerialTrafficControlScaffold();
        tc.setAdapterMemo(memo);
        memo.setTrafficController(tc);
        memo.setSerialAddress(new SerialAddress(memo));
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
