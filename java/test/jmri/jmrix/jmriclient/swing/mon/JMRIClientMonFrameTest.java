package jmri.jmrix.jmriclient.swing.mon;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of JMRIClientMonFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class JMRIClientMonFrameTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMRIClientMonFrame action = new JMRIClientMonFrame(new JMRIClientSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
