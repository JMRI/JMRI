package jmri.jmrix.can.cbus.swing.bootloader;

import org.junit.Assert;
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
        Assert.assertNotNull("exists",r);
        Assert.assertTrue("Record data length", r.data.length == HexRecord.MAX_LEN);
    }
        
}
