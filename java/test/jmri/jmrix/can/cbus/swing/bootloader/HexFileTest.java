package jmri.jmrix.can.cbus.swing.bootloader;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the HexFile class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
public class HexFileTest {

    @Test
    public void testCTor() {
        HexFile f = new HexFile("cbusHexFileTest");
        Assertions.assertNotNull( f, "exists");
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
