package jmri.jmrix.can.cbus.swing.bootloader;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the CbusPicHexFile class
 *
 * @author Bob Andrew Crosland (C) 2022
 */
public class CbusPicHexFileTest {

    @Test
    public void testCTor() {
        HexFile f = new HexFile("cbusPicHexFileTest");
        Assertions.assertNotNull(f, "exists");
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
