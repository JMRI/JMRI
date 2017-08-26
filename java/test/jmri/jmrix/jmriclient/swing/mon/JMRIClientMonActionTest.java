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

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMRIClientMonAction action = new JMRIClientMonAction("IEEE 802.15.4 test Action", new JMRIClientSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMRIClientMonAction action = new JMRIClientMonAction( new JMRIClientSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
