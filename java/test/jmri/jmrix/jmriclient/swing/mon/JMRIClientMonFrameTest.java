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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
