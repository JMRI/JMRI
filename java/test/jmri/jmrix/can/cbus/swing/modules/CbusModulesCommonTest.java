package jmri.jmrix.can.cbus.swing.modules;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of CbusModulesCommon
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CbusModulesCommonTest {
    
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testCtor() {

        CbusModulesCommon t = new CbusModulesCommon();
        Assert.assertNotNull("exists",t);
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
