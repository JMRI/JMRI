package jmri.jmrix.can.nmranet.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of NmraNetComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class NmraNetComponentFactoryTest {

    // private TrafficController tc = null;
    private CanSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        NmraNetComponentFactory action = new NmraNetComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // tc = new TestTrafficController();
        m = new CanSystemConnectionMemo();
        m.setSystemPrefix("ABC");

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
        // tc = null;
    }
}
