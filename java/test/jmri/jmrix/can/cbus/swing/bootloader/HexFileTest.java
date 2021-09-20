package jmri.jmrix.can.cbus.swing.bootloader;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the HexFile class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
public class HexFileTest {

    @Test
    public void testCTor() {
        HexFile f = new HexFile("cbusHexFileTest", 0xF00000);
        Assert.assertNotNull("exists",f);
    }
    
}
