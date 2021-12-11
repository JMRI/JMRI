package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusNodeInfoPane
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CbusNodeNVEditGuiPaneTest {
    
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNodeNVEditGuiPane t = new CbusNodeNVEditGuiPane(null);
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
