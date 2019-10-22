package jmri.jmrix.jmriclient.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;
import jmri.jmrix.jmriclient.JMRIClientTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JMRIClientMenu
 *
 * @author	Paul Bender Copyright (C) 2016
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // tc = new JMRIClientTrafficController();
        m = new JMRIClientSystemConnectionMemo();
        m.setSystemPrefix("ABC");

    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
        // tc = null;
    }
}
