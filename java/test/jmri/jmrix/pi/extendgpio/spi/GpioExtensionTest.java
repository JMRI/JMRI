/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.pi.extendgpio.spi;

import jmri.jmrix.pi.extendgpio.ExtensionService;
import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the Raspberry Pi GpioExtension class.
 *
 * @author   Dave Jordan
 */
public class GpioExtensionTest {
    
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
