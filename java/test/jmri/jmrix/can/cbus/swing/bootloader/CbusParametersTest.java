package jmri.jmrix.can.cbus.swing.bootloader;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the CbusParameters class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
public class CbusParametersTest {

    @Test
    public void testCTor() {
        CbusParameters p = new CbusParameters();
        Assertions.assertNotNull( p, "exists");
        Assertions.assertEquals( 33, p.paramData.length, "Param Data length");
    }

    @Test
    public void testIsValidIndex() {
        CbusParameters p = new CbusParameters();
        Assertions.assertTrue( p.isValidIndex(0), "index 0");
        Assertions.assertTrue( p.isValidIndex(32), "index 32");
        Assertions.assertFalse( p.isValidIndex(33), "index 33");
        Assertions.assertFalse( p.isValidIndex(-1), "index -1");
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
