    package jmri.jmrix.jmriclient.swing.mon;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of JMRIClientMonAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JMRIClientMonActionTest {

    private JMRIClientSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMRIClientMonAction action = new JMRIClientMonAction();
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new JMRIClientSystemConnectionMemo();
        jmri.InstanceManager.setDefault(JMRIClientSystemConnectionMemo.class,memo);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
