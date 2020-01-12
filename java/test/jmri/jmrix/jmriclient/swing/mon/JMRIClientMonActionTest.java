    package jmri.jmrix.jmriclient.swing.mon;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JMRIClientMonAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class JMRIClientMonActionTest {
	
    private JMRIClientSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMRIClientMonAction action = new JMRIClientMonAction();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new JMRIClientSystemConnectionMemo();
        jmri.InstanceManager.setDefault(JMRIClientSystemConnectionMemo.class,memo);
    }

    @After
    public void tearDown() {
	    memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	JUnitUtil.tearDown();
    }
}
