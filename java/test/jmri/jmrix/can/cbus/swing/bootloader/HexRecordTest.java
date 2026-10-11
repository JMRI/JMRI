package jmri.jmrix.can.cbus.swing.bootloader;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the HexRecord class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
public class HexRecordTest {

    @Test
    public void testCTor() {
        HexRecord r = new HexRecord();
        Assertions.assertNotNull( r, "exists");
        Assertions.assertEquals( HexRecord.MAX_LEN, r.data.length, "Record data length");
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
