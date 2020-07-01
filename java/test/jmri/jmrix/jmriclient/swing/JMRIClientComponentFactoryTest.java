package jmri.jmrix.jmriclient.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of JMRIClientComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JMRIClientComponentFactoryTest {

    // private JMRIClientTrafficController tc = null;
    private JMRIClientSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        JMRIClientComponentFactory action = new JMRIClientComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // tc = new JMRIClientTrafficController();
        m = new JMRIClientSystemConnectionMemo();
        m.setSystemPrefix("ABC");

    }

    @AfterEach
    public void tearDown() {
        m.getJMRIClientTrafficController().terminateThreads();
        JUnitUtil.tearDown();
    }
}
