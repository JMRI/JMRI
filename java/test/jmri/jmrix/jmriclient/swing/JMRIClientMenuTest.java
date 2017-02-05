package jmri.jmrix.jmriclient.swing;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.jmriclient.JMRIClientTrafficController;
import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of JMRIClientMenu
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class JMRIClientMenuTest {


    private JMRIClientTrafficController tc = null;
    private JMRIClientSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        JMRIClientMenu action = new JMRIClientMenu(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new JMRIClientTrafficController();
        m = new JMRIClientSystemConnectionMemo();
        m.setSystemPrefix("ABC");

    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
        tc = null;
    }
}
