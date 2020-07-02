package jmri.jmrix.can.cbus.swing.bootloader;

import org.junit.Assert;
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
        Assert.assertNotNull("exists",p);
        Assert.assertTrue("Param Data length", p.paramData.length == 33);
    }
    
}
