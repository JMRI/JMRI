/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.pi.extendgpio;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the Raspberry Pi ExtensionService class.
 *
 * @author   Dave Jordan
 */
public class ExtensionServiceTest {
    
    @Test
    public void testInstantiation(){
      Assert.assertNotNull("ExtensionService Instantiation", ExtensionService.getInstance());
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
