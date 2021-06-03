/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.pi.extendgpio;

import jmri.jmrix.pi.extendgpio.spi.GpioExtension;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the Raspberry Pi ExtensionService class.
 *
 * @author   Dave Jordan
 */
public class MCP23017GpioExtensionTest {
    
    @Test
    public void testExtensionName(){
        GpioExtension ex = ExtensionService.getExtensionFromSystemName ("PT:MCP23017:");
        String s = ex.getExtensionName();
        Assert.assertEquals ("MCP23017", s);
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
