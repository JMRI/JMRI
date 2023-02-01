package jmri.jmrix.powerline;

import jmri.Manager.NameValidity;
import jmri.jmrix.powerline.simulator.SpecificSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * JUnit tests for the SerialAddress utility class.
 *
 * @author Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2007, 2008
 */
public class SerialAddressTest {

    SerialTrafficControlScaffold tc = null;

    public void testIsX10True() {
        Assert.assertTrue("valid config PLA1", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PLA1", 'L'));
        Assert.assertTrue("valid config PLB7", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PLB7", 'L'));
        Assert.assertTrue("valid config PSK16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PSK16", 'S'));
        Assert.assertTrue("valid config PSP16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PSP16", 'S'));
        Assert.assertTrue("valid config PTK16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PTK16", 'T'));
        Assert.assertTrue("valid config PTP16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PTP16", 'T'));
        Assert.assertEquals("valid format - PLA1", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLA1", 'L'));
        Assert.assertEquals("valid format - PLA16", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLA16", 'L'));
        Assert.assertEquals("valid format - PLK3", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLK3", 'L'));
        Assert.assertEquals("valid format - PTA1", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTA1", 'T'));
        Assert.assertEquals("valid format - PTA16", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTA16", 'T'));
        Assert.assertEquals("valid format - PTK3", NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTK3", 'T'));
    }

    public void testIsX10False() {
        Assert.assertFalse("invalid config PL4007", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PL4007", 'L'));
        Assert.assertFalse("invalid config PL10033", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PL10033", 'L'));
        Assert.assertEquals("invalid format - PL2", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2", 'L'));
        Assert.assertEquals("invalid format - PL0B2", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL0B2", 'L'));
        Assert.assertEquals("invalid format - PL", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL", 'L'));
        Assert.assertEquals("invalid format - PLB", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLB", 'L'));
        Assert.assertEquals("invalid format - PL2B5x", NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2B5x", 'L'));
    }


    public void testIsInsteonTrue() {
        Assert.assertTrue("PL01.02.03", tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.02.03"));
        Assert.assertTrue("PLA1.02.03", tc.getAdapterMemo().getSerialAddress().isInsteon("PLA1.02.03"));
        Assert.assertTrue("PLA1.A2.03", tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.A2.03"));
        Assert.assertTrue("PLA1.02.A3", tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.02.A3"));
    }


    public void testIsInsteonFalse() {
        Assert.assertFalse("PLA1", tc.getAdapterMemo().getSerialAddress().isInsteon("PLA1"));
    }

    public void testIsDmx512True() {
        Assert.assertTrue("PL1", tc.getAdapterMemo().getSerialAddress().isInsteon("PL1"));
        Assert.assertTrue("PL256", tc.getAdapterMemo().getSerialAddress().isInsteon("PL256"));
        Assert.assertTrue("PL512", tc.getAdapterMemo().getSerialAddress().isInsteon("PL512"));
    }

    public void testIsDmx512False() {
        Assert.assertFalse("PLA1", tc.getAdapterMemo().getSerialAddress().isInsteon("PLA1"));
        Assert.assertFalse("PL0", tc.getAdapterMemo().getSerialAddress().isInsteon("PL0"));
        Assert.assertFalse("PL513", tc.getAdapterMemo().getSerialAddress().isInsteon("PL513"));
        Assert.assertFalse("PL1110", tc.getAdapterMemo().getSerialAddress().isInsteon("PL1110"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();        
        SpecificSystemConnectionMemo memo = new SpecificSystemConnectionMemo();
        // prepare an interface, register
        tc = new SerialTrafficControlScaffold();
        tc.setAdapterMemo(memo);
        memo.setTrafficController(tc);
        memo.setSerialAddress(new SerialAddress(memo));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
