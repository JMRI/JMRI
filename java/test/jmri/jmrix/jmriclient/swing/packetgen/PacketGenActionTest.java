package jmri.jmrix.jmriclient.swing.packetgen;

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
 * Test simple functioning of PacketGenAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class PacketGenActionTest {

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketGenAction action = new PacketGenAction("JMRIClient PacketGen test Action", new JMRIClientSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketGenAction action = new PacketGenAction( new JMRIClientSystemConnectionMemo());
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
