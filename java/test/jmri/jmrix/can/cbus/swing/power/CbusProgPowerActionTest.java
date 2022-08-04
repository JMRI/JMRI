package jmri.jmrix.can.cbus.swing.power;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test CBUS programming track power action
 * 
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusProgPowerActionTest {
    
    @Test
    public void testCTor() {
        CbusProgPowerAction t = new CbusProgPowerAction();
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
