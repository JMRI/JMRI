package jmri.jmrix.can.cbus.swing.bootloader;

import org.junit.*;

/**
 * Tests for the CbusPicHexFile class
 *
 * @author Bob Andrew Crosland (C) 2022
 */
public class CbusPicHexFileTest {

    @org.junit.jupiter.api.Test
    public void testCTor() {
        HexFile f = new HexFile("cbusPicHexFileTest");
        Assert.assertNotNull("exists",f);
    }
    
}
