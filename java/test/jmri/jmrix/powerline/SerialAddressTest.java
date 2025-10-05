package jmri.jmrix.powerline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Manager.NameValidity;
import jmri.jmrix.powerline.simulator.SpecificSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SerialAddress utility class.
 *
 * @author Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2007, 2008
 */
public class SerialAddressTest {

    private SerialTrafficControlScaffold tc = null;

    @Test
    public void testIsX10True() {
        assertTrue( tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PLA1", 'L'), "valid config PLA1");
        assertTrue( tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PLB7", 'L'), "valid config PLB7");
        assertTrue( tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PSK16", 'S'), "valid config PSK16");
        assertTrue( tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PSP16", 'S'), "valid config PSP16");
        assertTrue( tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PTK16", 'T'), "valid config PTK16");
        assertTrue( tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PTP16", 'T'), "valid config PTP16");
        assertEquals( NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLA1", 'L'), "valid format - PLA1");
        assertEquals( NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLA16", 'L'), "valid format - PLA16");
        assertEquals( NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLK3", 'L'), "valid format - PLK3");
        assertEquals( NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTA1", 'T'), "valid format - PTA1");
        assertEquals( NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTA16", 'T'), "valid format - PTA16");
        assertEquals( NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTK3", 'T'), "valid format - PTK3");
    }

    @Test
    public void testIsX10False() {
        assertFalse( tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PL4007", 'L'), "invalid config PL4007");
        assertFalse( tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PL10033", 'L'), "invalid config PL10033");

        // 5.13.5 the following assertion changed to match actual behaviour
        assertEquals( NameValidity.VALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2", 'L'), "invalid format - PL2");

        assertEquals( NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL0B2", 'L'), "invalid format - PL0B2");
        assertEquals( NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL", 'L'), "invalid format - PL");
        assertEquals( NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLB", 'L'), "invalid format - PLB");
        assertEquals( NameValidity.INVALID, tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2B5x", 'L'), "invalid format - PL2B5x");
    }

    @Test
    public void testIsInsteonTrue() {
        assertTrue( tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.02.03"), "PL01.02.03");
        assertTrue( tc.getAdapterMemo().getSerialAddress().isInsteon("PLA1.02.03"), "PLA1.02.03");
        assertTrue( tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.A2.03"), "PLA1.A2.03");
        assertTrue( tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.02.A3"), "PLA1.02.A3");
    }

    @Test
    public void testIsInsteonFalse() {
        assertFalse( tc.getAdapterMemo().getSerialAddress().isInsteon("PLA1"), "PLA1");
    }

    @Test
    public void testIsDmx512True() {
        assertTrue( tc.getAdapterMemo().getSerialAddress().isInsteon("PL1"), "PL1");
        assertTrue( tc.getAdapterMemo().getSerialAddress().isInsteon("PL256"), "PL256");
        assertTrue( tc.getAdapterMemo().getSerialAddress().isInsteon("PL512"), "PL512");
    }

    @Test
    public void testIsDmx512False() {
        assertFalse( tc.getAdapterMemo().getSerialAddress().isInsteon("PLA1"), "PLA1");
        assertFalse( tc.getAdapterMemo().getSerialAddress().isInsteon("PL0"), "PL0");
        assertFalse( tc.getAdapterMemo().getSerialAddress().isInsteon("PL513"), "PL513");
        assertFalse( tc.getAdapterMemo().getSerialAddress().isInsteon("PL1110"), "PL1110");
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
        tc.terminateThreads();
        JUnitUtil.tearDown();
    }

}
