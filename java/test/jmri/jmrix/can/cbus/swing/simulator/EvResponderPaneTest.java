package jmri.jmrix.can.cbus.swing.simulator;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of EvResponderPane
 *
 * @author Steve Young Copyright (C) 2019
 */
public class EvResponderPaneTest  {

    @Test
    public void testCTor() {
        EvResponderPane t = new EvResponderPane(null);
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
