package jmri.jmrix.jmriclient.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of JMRIClientMenu
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JMRIClientMenuTest {


    // private JMRIClientTrafficController tc = null;
    private JMRIClientSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        JMRIClientMenu action = new JMRIClientMenu(m);
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
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
        // tc = null;
    }
}
