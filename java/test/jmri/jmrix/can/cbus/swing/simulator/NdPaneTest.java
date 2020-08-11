package jmri.jmrix.can.cbus.swing.simulator;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of NdPane
 *
 * @author Steve Young Copyright (C) 2019
 */
public class NdPaneTest  {

    @Test
    public void testCTor() {
        NdPane t = new NdPane();
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
